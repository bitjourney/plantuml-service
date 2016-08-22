
# PlantUML HTTP service [![CircleCI](https://circleci.com/gh/bitjourney/plantuml-service.svg?style=svg)](https://circleci.com/gh/bitjourney/plantuml-service)

This is an HTTP interface to [PlantUML](http://plantuml.com/).

## Usage

### `GET /svg/:source`

Returns an SVG image of `:source` processed by PlantUML.

`:source` is source code encoded in [PlantUML Text Encoding](http://plantuml.com/pte.html).

This is compatible with `plantuml-server` (e.g. `http://plantuml.com/plantuml/svg/SyfFKj2rKt3CoKnELR1Io4ZDoSa70000`).

Example:

```markdown
![example](https://plantuml-service.herokuapp.com/svg/SyfFKj2rKt3CoKnELR1Io4ZDoSa70000)
```

![example](https://plantuml-service.herokuapp.com/svg/SyfFKj2rKt3CoKnELR1Io4ZDoSa70000)

https://plantuml-service.herokuapp.com/svg/SyfFKj2rKt3CoKnELR1Io4ZDoSa70000

### `GET /version`

Shows PlantUML version in JSON:

```json
{"PlantUML":"8046"}
```

This is also intended to check the service helth.

Example: https://plantuml-service.herokuapp.com/version

## Development

Run on local:

```sh
./gradlew stage && heroku local:start
```

Production:

```sh
java -jar ./build/libs/plantuml-1.0-SNAPSHOT.jar $PORT
```

## For macOS

There is a homebrew formula in [bitjourney/homebrew-self](https://github.com/bitjourney/homebrew-self):

```sh
brew install bitjourney/self/plantuml-service
brew services start bitjourney/self/plantuml-service
```

## See Also

* https://github.com/plantuml/plantuml-server

## Author

FUJI Goro ([gfx](https://github.com/gfx)).

## License

Copyright (c) 2016 Bit Journey, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
