# Техническое задание в LaTeX

Каталог содержит редактируемое ТЗ на Android-приложение для складских и торговых операций.

## Состав

- `main.tex` - точка входа и порядок разделов;
- `preamble.tex` - оформление, шрифты и команды документа;
- `sections/` - разделы ТЗ, которые можно менять независимо;
- `Makefile` - повторяемая сборка и очистка временных файлов.

Готовый PDF создается в `output/pdf/warehouse-android-technical-specification.pdf`.

## Сборка

Из корня проекта:

```sh
make -C docs/technical-specification pdf
```

Требуется XeLaTeX и шрифты PT Sans/PT Mono. На macOS они обычно доступны вместе с системой и MacTeX; на Ubuntu/Debian установите `texlive-xetex`, `texlive-lang-cyrillic` и `fonts-pt-sans`.

Ручной эквивалент команды:

```sh
mkdir -p output/pdf
cd docs/technical-specification
xelatex -interaction=nonstopmode -halt-on-error -jobname=warehouse-android-technical-specification -output-directory=../../output/pdf main.tex
xelatex -interaction=nonstopmode -halt-on-error -jobname=warehouse-android-technical-specification -output-directory=../../output/pdf main.tex
```

Два запуска нужны для корректного оглавления и внутренних ссылок.
