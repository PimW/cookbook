# cookbook

A web application written in Clojure that can be used to view recipes written in [Cooklang](https://cooklang.org/docs/spec/).

## Usage

Currently recipes are stored in plain-text (cooklang format) in the resources folder. Each recipe can be parsed using the cooklang parser and added to the application with unique urls as seen in `api/recipe`.

The application can be installed and ran using `leiningen`.

## Structure

Recipes are stored in plaintext and parsed using the cooklang parsed. While the application is running they are stored in memory.
The spec for the internal structure of the recipe can be found in `/spec.clj`.

The webserver is based on `ring` using the jetty adapter. Routing is handled using `compojure` and the html is rendered with `hiccup`/

## Potential future features

- [ ] Search or navigation based on tags (cuisine, course, etc.).
- [ ] Import of online recipes.
- [ ] Adding and editing recipes.
- [ ] Version control and viewing changes to recipes.

