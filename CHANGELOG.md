# Changelog

All notable changes to this project will be documented in this file.

## [1.1.0-ALPHA] - Unreleased

### Added
- **Foldered World Creation (Milestone A)**: Realms are now created as proper world folders in a configurable server sub-directory (`/realms` by default).
  - The world template is copied asynchronously to prevent server lag during creation.
  - Includes lock-file protection to prevent concurrent creations by the same user.
  - On failure, the system now automatically rolls back changes and deletes partial world folders.
  - Realm metadata (`name`, `owner`, `worldName`, `template`) is now persisted in `worlds.yml`.
  - Added name sanitization and a configurable world folder naming format.
  - `/realms create` now accepts a template name, with tab-completion for available templates.
- New configuration options under the `realms:` block in `config.yml` to manage the new creation process.

### Changed
- The `/realms create` command now uses the new asynchronous, foldered creation system.
- The `Realm` data object now stores additional metadata, including `worldName`, `template`, and `createdAt`.
- Data saving to `worlds.yml` is now performed asynchronously to reduce main-thread I/O.

### Fixed
- Stabilized the realm creation process to be more resilient to errors and prevent partial or corrupt data.