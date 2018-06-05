# Change Log
All notable changes to this project will be documented in this file.

## [v0.1.3]
### Changed
- Travis now only runs deployments to GitHub and Maven Central for
  tagged commits.

## [v0.1.2]
### Fixed
- Fixed the failing Travis build related to a Nexus write error.

## [v0.1.1]
### Changed
- The DEFCoN-ImageJ jars are now hosted on Maven Central.

## [v0.1.0]
### Added
- Two new methods were added to the `Predictor` interface:
  `getLocalCountMap()` and `getMaximumLocalCount()`. These allow for a
  more flexible means for computing maximum local counts than the
  TensorFlow implementation.

## [v0.0.2]
### Added

- The `DefaultPredictor` class now automatically crops the input image
  to dimensions that are a multiple of four.
  
### Changed
- Moved the exceptions associated with the `Predictor` interface into
  the public `predictors` package.

## [v0.0.1]
### Changed

- Decoupled the core density map estimation routine into a `Predictor`
  interface and `DefaultPredictor` implementation.

## [v0.0.0]
### Added

- Initial project files.

[Unreleased]: https://github.com/LEB-EPFL/DEFCoN-ImageJ/compare/v0.1.3...HEAD
[v0.1.3]: https://github.com/LEB-EPFL/DEFCoN-ImageJ/releases/tag/0.1.3
[v0.1.2]: https://github.com/LEB-EPFL/DEFCoN-ImageJ/releases/tag/0.1.2
[v0.1.1]: https://github.com/LEB-EPFL/DEFCoN-ImageJ/releases/tag/0.1.1
[v0.1.0]: https://github.com/LEB-EPFL/DEFCoN-ImageJ/releases/tag/0.1.0
[v0.0.2]: https://github.com/LEB-EPFL/DEFCoN-ImageJ/releases/tag/0.0.2
[v0.0.1]: https://github.com/LEB-EPFL/DEFCoN-ImageJ/releases/tag/0.0.1
[v0.0.0]: https://github.com/LEB-EPFL/DEFCoN-ImageJ/releases/tag/0.0.0
