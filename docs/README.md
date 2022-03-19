## Install required dependencies

```bash
sudo apt install -y ruby ruby-dev graphviz
```

You have to install [bundler](https://bundler.io) first if needed:

```bash
gem install bundler
```

Then install all required dependencies to generate documentation through [Asciidoctor](https://asciidoctor.org).

```bash
bundle
```

## Generating documents

```bash
./convert
```

## Editing with Live Preview

```bash
bundle exec guard
```
