# Occtax-mobile

![PNV](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/pnv/res/mipmap-xxxhdpi/ic_launcher.png)
![PNE](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/pne/res/mipmap-xxxhdpi/ic_launcher.png)

GeoNature Android mobile application for Occtax module.

## Upgrade git sub modules

Do **NOT** modify directly any git sub modules (e.g. `commons`, `mountpoint`, `viewpager` and `maps`).
Any changes should be made from each underlying git repository:

* `commons`: [gn_mobile_core](https://github.com/PnX-SI/gn_mobile_core) git repository
* `mountpoint`: [gn_mobile_core](https://github.com/PnX-SI/gn_mobile_core) git repository
* `viewpager`: [gn_mobile_core](https://github.com/PnX-SI/gn_mobile_core) git repository
* `maps`: [gn_mobile_maps](https://github.com/PnX-SI/gn_mobile_maps) git repository

```bash
./upgrade_submodules.sh
```

## Troubleshooting

* Kotlin error, Redeclaration from class within imported module:

  clean project from menu *Build -> Clean Project*, then rebuild project.

## Full Build

A full build can be executed with the following command:

```
./gradlew clean assembleDebug
```
