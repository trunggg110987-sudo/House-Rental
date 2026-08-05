package vn.codegym.house_rental.security.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.repository.UserRepository;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        User.AuthProvider provider;
        if ("google".equalsIgnoreCase(registrationId)) {
            provider = User.AuthProvider.GOOGLE;
        } else if ("facebook".equalsIgnoreCase(registrationId)) {
            provider = User.AuthProvider.FACEBOOK;
        } else {
            provider = User.AuthProvider.LOCAL;
        }

        Map<String, Object> attributes = oauth2User.getAttributes();
        String providerId = null;
        String email = null;
        String fullName = null;
        String avatar = null;

        if (provider == User.AuthProvider.GOOGLE) {
            providerId = (String) attributes.get("sub");
            email = (String) attributes.get("email");
            fullName = (String) attributes.get("name");
            avatar = (String) attributes.get("picture");
        } else if (provider == User.AuthProvider.FACEBOOK) {
            providerId = (String) attributes.get("id");
            email = (String) attributes.get("email");
            fullName = (String) attributes.get("name");

            if (attributes.containsKey("picture")) {
                Object pictureObj = attributes.get("picture");
                if (pictureObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> pictureMap = (Map<String, Object>) pictureObj;
                    if (pictureMap.containsKey("data")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> dataMap = (Map<String, Object>) pictureMap.get("data");
                        if (dataMap.containsKey("url")) {
                            avatar = (String) dataMap.get("url");
                        }
                    }
                }
            }
        }

        User user = processOAuth2User(provider, providerId, email, fullName, avatar);
        return new CustomOAuth2User(oauth2User, user);
    }

    private User processOAuth2User(User.AuthProvider provider, String providerId, String email, String fullName, String avatar) {
        Optional<User> userOptional = userRepository.findByProviderAndProviderId(provider, providerId);

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            if (fullName != null && !fullName.isBlank()) {
                existingUser.setFullName(fullName);
            }
            if (avatar != null && !avatar.isBlank()) {
                existingUser.setAvatar(avatar);
            }
            if (email != null && existingUser.getEmail() == null) {
                existingUser.setEmail(email);
            }
            return userRepository.save(existingUser);
        }

        // Try finding by email if email is present
        if (email != null && !email.isBlank()) {
            Optional<User> userByEmail = userRepository.findByEmail(email);
            if (userByEmail.isPresent()) {
                User existingUser = userByEmail.get();
                existingUser.setProvider(provider);
                existingUser.setProviderId(providerId);
                if (avatar != null && !avatar.isBlank()) {
                    existingUser.setAvatar(avatar);
                }
                return userRepository.save(existingUser);
            }
        }

        // Create new OAuth2 user
        String username = email != null && !email.isBlank() ? email : provider.name().toLowerCase() + "_" + providerId;

        // Ensure unique username
        if (userRepository.existsByUsername(username)) {
            username = provider.name().toLowerCase() + "_" + providerId;
        }

        User newUser = User.builder()
                .username(username)
                .email(email)
                .fullName(fullName != null ? fullName : username)
                .avatar(avatar)
                .provider(provider)
                .providerId(providerId)
                .role(User.Role.ROLE_USER)
                .build();

        return userRepository.save(newUser);
    }
}
