# Change Log
All notable changes to this project will be documented in this file.

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

[v0.0.2]: https://github.com/LEB-EPFL/DEFCoN-ImageJ/releases/tag/0.0.2
[v0.0.1]: https://github.com/LEB-EPFL/DEFCoN-ImageJ/releases/tag/0.0.1
[v0.0.0]: https://github.com/LEB-EPFL/DEFCoN-ImageJ/releases/tag/0.0.0
