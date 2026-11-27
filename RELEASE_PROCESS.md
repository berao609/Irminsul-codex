# Release Process

This document describes how to version, build, sign, and publish Irminsul Codex Android releases.

## 1. Versioning

The application version is defined in the repository-root `version.properties` file:

```properties
VERSION_CODE=2
VERSION_NAME=1.0.0
```

- `VERSION_NAME` is the human-readable Semantic Versioning value: `MAJOR.MINOR.PATCH`.
- `VERSION_CODE` is the Android integer reserved for official Release APKs. Increase it for every official release. Never reuse or decrease a published release version code.
- Keep the version name and version code in the same change.

The Debug CI workflow automatically assigns a separate development version to every new workflow run:
- Debug version name: `VERSION_NAME-dev.GITHUB_RUN_NUMBER` (for example, `1.0.0-dev.80`).
- Debug version code: `VERSION_CODE + GITHUB_RUN_NUMBER`.
- GitHub Actions run numbers increase for each new run of the Debug workflow, whether triggered by a push to `main` or manually.
- Re-running an existing workflow run keeps the same run number and version code. Start a new workflow run to produce a newer installable Debug APK.
- The CI values are injected through environment variables and do not modify `version.properties`.
- Official Release builds ignore these CI overrides and use the exact values in `version.properties` and the matching version tag.

Debug and Release APKs use different signing certificates. Moving between them requires a one-time uninstall/reinstall regardless of version code. Within the Debug channel, consecutive CI builds use the same persistent Debug certificate and increasing version codes, so they can update in place.

Version number guidance:

| Change type | Example | When to use |
|---|---|---|
| PATCH | 1.0.0 → 1.0.1 | Backward-compatible fixes |
| MINOR | 1.0.0 → 1.1.0 | Backward-compatible features |
| MAJOR | 1.0.0 → 2.0.0 | Major incompatible changes |

The first configured release is `1.0.0` with version code `2`, which is greater than the existing app version code `1`.

## 2. Debug builds versus official releases

### Debug builds

- Every push to `main` triggers the Debug APK workflow.
- A manual run is also available through GitHub Actions.
- The artifact name includes the app version and version code.
- Debug artifacts are development builds, not official releases.
- Debug artifacts are retained for 14 days by the workflow.

### Official releases

- Official releases are created only by pushing a version tag such as `v1.0.0`.
- The tag must exactly match `VERSION_NAME` in the tagged source.
- The release workflow runs `gradle testDebugUnitTest --no-daemon` against the tagged source before decoding the Release keystore or building/signing the APK. A failing unit test stops the job before publication.
- After the test gate passes, the workflow builds a signed Release APK, verifies its signature, calculates a SHA-256 checksum, and publishes both files in a GitHub Release.
- Room instrumentation tests are not repeated for every official release. They remain part of the Debug CI gate for relevant persistence/schema changes and manual full-test runs.
- The release workflow stops if required signing secrets are missing or the tag and version metadata do not match.

## 3. One-time signing key setup

Android updates must use the same application ID and signing certificate as the installed release. The application ID is currently `com.irminsul.codex`.

Create one permanent upload/release keystore and keep a secure offline backup. Do not regenerate it for later releases. Losing the key can prevent users from installing future updates over the existing signed app.

You can create the keystore using Android Studio's **Build → Generate Signed Bundle / APK → APK → Create new** flow, or with `keytool`:

```bash
keytool -genkeypair -v \
  -keystore irminsul-release.jks \
  -alias irminsul-release \
  -keyalg RSA -keysize 2048 -validity 10000
```

Store the keystore and its passwords somewhere secure and backed up. Never commit the keystore, its passwords, or a base64 copy to the repository.

### Configure GitHub Actions secrets

In the repository, open **Settings → Secrets and variables → Actions → New repository secret** and add these four secrets:

| Secret name | Value |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | Base64-encoded bytes of the permanent `.jks` keystore |
| `RELEASE_KEYSTORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | Alias used when creating the key |
| `RELEASE_KEY_PASSWORD` | Password for that key |

Example base64 encoding commands:

Linux:
```bash
base64 -w 0 irminsul-release.jks
```

macOS:
```bash
base64 < irminsul-release.jks | tr -d '\n'
```

PowerShell:
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("irminsul-release.jks"))
```

Copy the output directly into the `RELEASE_KEYSTORE_BASE64` secret. Do not paste the keystore or passwords into chat, issues, commits, or workflow logs.

## 4. Publishing a release

For each official release:

1. Finish and review the code changes on `main`.
2. Edit `version.properties`:
   - Choose the next `VERSION_NAME`.
   - Increase `VERSION_CODE` by at least one.
3. Commit and push the version change to `main`.
4. Confirm all four GitHub Actions signing secrets are configured.
5. Create and push a matching Git tag. For example, if the file says `VERSION_NAME=1.0.1`, create tag `v1.0.1`.
6. Open the repository's **Actions** tab and monitor **Publish Android Release**. Confirm the tagged-source unit-test gate passes before the signing/build steps proceed.
7. Confirm the workflow succeeds and verify that the GitHub Release contains:
   - `app-release.apk`
   - `app-release.apk.sha256`
8. Download and install the APK on a test device before distributing it.

The workflow validates the tag against the version metadata. A mismatch fails the release rather than publishing a mislabeled APK.

## 5. Installing updates

For an installed app to update in place, all of these must remain true:

- The application ID stays `com.irminsul.codex`.
- The APK is signed with the same permanent release key.
- The new `versionCode` is greater than the installed version code.

The first transition from a Debug APK to the official Release APK may require uninstalling the Debug app because Debug and Release use different signing certificates. Uninstalling can erase app-local data. Back up any important data before switching.

After the first official Release APK is installed, future APKs signed with the same permanent release key and a higher version code should install as updates without uninstalling.

## 6. Current limitations and safeguards

- The repository currently has no Gradle Wrapper. CI uses the explicitly configured Gradle 8.14.8 distribution. Adding and validating the Gradle Wrapper is a separate build-foundation improvement.
- The Release workflow runs the tagged-source JVM unit-test gate before signing and publication. It does not run Room instrumentation tests on every release; those remain covered by the Debug CI policy for relevant changes.
- The workflow's successful tagged-release publication path still requires verification during the first controlled official release.
- The Release workflow verifies APK signing before publication, but the first release still needs a real-device installation/update test.
- The current app stores some account credentials in ordinary SharedPreferences. Release signing does not make that storage encrypted or production-secure.
- Do not create the first official version tag until the signing key is backed up, all four Actions secrets are configured, and the Release workflow can run.
