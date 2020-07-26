# Occtax-mobile

GeoNature Android mobile application for Occtax module.

Installation documentation (French) : https://github.com/PnX-SI/gn_mobile_occtax/blob/master/docs/installation-fr.md

## Launcher icons

| Name                                                                 | Flavor    | Launcher icon                                                                                                          |
| -------------------------------------------------------------------- | --------- | ---------------------------------------------------------------------------------------------------------------------- |
| Default                                                              | _generic_ | ![PNX](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/main/res/mipmap-xhdpi/ic_launcher.png) |
| [Parc National des Cévennes](http://www.cevennes-parcnational.fr)    | _pnc_     | ![PNC](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/pnc/res/mipmap-xhdpi/ic_launcher.png)  |
| [Parc National des Écrins](http://www.ecrins-parcnational.fr)        | _pne_     | ![PNE](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/pne/res/mipmap-xhdpi/ic_launcher.png)  |
| [Parc National du Mercantour](http://www.mercantour-parcnational.fr) | _pnm_     | ![PNE](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/pnm/res/mipmap-xhdpi/ic_launcher.png)  |
| [Parc National de la Vanoise](http://www.vanoise-parcnational.fr)    | _pnv_     | ![PNE](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/pnv/res/mipmap-xhdpi/ic_launcher.png)  |

## Settings

Example:

```json
{
  "area_observation_duration": 365,
  "map": {
    "show_scale": true,
    "show_compass": true,
    "max_bounds": [
      [47.253369, -1.605721],
      [47.173845, -1.482811]
    ],
    "center": [47.225827, -1.55447],
    "start_zoom": 10.0,
    "min_zoom": 8.0,
    "max_zoom": 19.0,
    "min_zoom_editing": 12.0,
    "layers": [
      {
        "label": "Nantes",
        "source": "nantes.mbtiles"
      }
    ]
  }
}
```

### Parameters description

| Parameter                   | UI      | Description                                                                    | Default value |
| --------------------------- | ------- | ------------------------------------------------------------------------------ | ------------- |
| `area_observation_duration` | &#9744; | Area observation duration period (in days)                                     | 365           |
| `map`                       | &#9744; | Maps settings (cf. https://github.com/PnX-SI/gn_mobile_maps/tree/develop/maps) |               |

## Upgrade git sub modules

Do **NOT** modify directly any git sub modules (e.g. `commons`, `mountpoint`, `viewpager` and `maps`).
Any changes should be made from each underlying git repository:

- `commons`: [gn_mobile_core](https://github.com/PnX-SI/gn_mobile_core) git repository
- `mountpoint`: [gn_mobile_core](https://github.com/PnX-SI/gn_mobile_core) git repository
- `viewpager`: [gn_mobile_core](https://github.com/PnX-SI/gn_mobile_core) git repository
- `maps`: [gn_mobile_maps](https://github.com/PnX-SI/gn_mobile_maps) git repository

```bash
./upgrade_submodules.sh
```

## Troubleshooting

- Kotlin error, Redeclaration from class within imported module:

  clean project from menu _Build -> Clean Project_, then rebuild project.

## Full Build

A full build can be executed with the following command:

```
./gradlew clean assembleDebug
```
