# Occtax-mobile

![Occtax-mobile launcher](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/main/res/mipmap-xxxhdpi/ic_launcher.png)

GeoNature Android mobile application for Occtax module.

## Upgrade git sub modules

Do **NOT** modify directly any git sub modules (e.g. `commons`, `viewpager` and `maps`).
Any changes should be made from each underlying git repository:

* `commons`: [gn_mobile_core](https://github.com/PnX-SI/gn_mobile_core) git repository
* `viewpager`: [gn_mobile_core](https://github.com/PnX-SI/gn_mobile_core) git repository
* `maps`: [gn_mobile_maps](https://github.com/PnX-SI/gn_mobile_maps) git repository

```bash
./upgrade_submodules.sh
```

## Troubleshooting

* Kotlin error, Redeclaration from class within imported module:

  clean project from menu *Build -> Clean Project*, then rebuild project.
