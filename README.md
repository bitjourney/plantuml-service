
# PlantUML HTTP service [![CircleCI](https://circleci.com/gh/gfx/plantuml-service.svg?style=svg)](https://circleci.com/gh/gfx/plantuml-service)

This is an HTTP interface to [PlantUML](http://plantuml.com/).

## Usage

### `GET /svg/:source`

Returns an SVG image of `:source` processed by PlantUML.

`:source` is source code encoded in [PlantUML Text Encoding](http://plantuml.com/pte.html).

Example:

```markdown
![example](https://plantuml-service.herokuapp.com/svg/SyfFKj2rKt3CoKnELR1Io4ZDoSa70000)
```

![example](https://plantuml-service.herokuapp.com/svg/SyfFKj2rKt3CoKnELR1Io4ZDoSa70000)


This is compatible with `plantuml-server` (e.g. `http://plantuml.com/plantuml/svg/SyfFKj2rKt3CoKnELR1Io4ZDoSa70000`).

### `GET /version`

Shows PlantUML version in JSON:

```json
{"PlantUML":"8046"}
```

This is intended to check the service helth.

## Run the Http Service

Local:

```
./gradlew stage && heroku local:start
```

Production:

```
java -jar ./build/libs/plantuml-1.0-SNAPSHOT.jar $PORT
```

## See Also

* https://github.com/plantuml/plantuml-server

## Author

FUJI Goro ([gfx](https://github.com/gfx)).

## License

Copyright (c) 2016 FUJI Goro (gfx).

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
