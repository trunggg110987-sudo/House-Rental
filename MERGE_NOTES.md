# House-Rental merged project

Base: `House-Rental-develop(2)` because it contains the most complete feature set in the supplied archives, including Admin/Host management, notifications and email.

Merged from the supplied versions:
- Added `src/main/resources/templates/auth/login.html` from the Trung version because the develop archive referenced `auth/login` but did not contain the template.
- Kept the session-based `AuthController`/`UserService.login()` flow from develop.
- Fixed `user/profile.html`: duplicate doctype, malformed markup, null-safe `hostStatus` expressions, and corrected the request-host action to `/profile/request-host`.
- Added a login-active-account check already present in `UserService.login()` so locked users cannot log in.
- Kept OAuth2/Spring Security files out of this merge because those archives use a different authentication architecture and would conflict with the session-based authentication in the develop version.
- Removed IDE/Gradle cache artifacts from the merged package.

Build verification could not be completed in this environment because the Gradle wrapper distribution was not available locally and external network access is disabled.
