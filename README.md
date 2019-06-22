# gn_mobile_occtax

## Upgrade git sub modules

Do **NOT** modify directly `commons` and `viewpager`. Any changes should be made from
[gn_mobile_core](https://github.com/PnX-SI/gn_mobile_core) git repository.

```bash
./upgrade_submodules.sh
```

## Troubleshooting

* Kotlin error, Redeclaration from class within imported module:

  clean project from menu *Build -> Clean Project*, then rebuild project.
