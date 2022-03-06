# Occtax-mobile

GeoNature Android mobile application for _Occtax_ GeoNature module.

Based on [datasync module](https://github.com/PnX-SI/gn_mobile_core) to synchronize data and
[maps module](https://github.com/PnX-SI/gn_mobile_maps) to show maps data.

## Documentation

- [Installation (in french)](./docs/installation-fr.adoc)
- [User documentation (in french)](./docs/utilisation-fr.md)

## Launcher icons

| Name                                                                 | Flavor    | Launcher icon                                                                                                                                                                                                                                                  |
| -------------------------------------------------------------------- | --------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Default                                                              | _generic_ | ![PNX](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/main/res/mipmap-xhdpi/ic_launcher.png) ![PNX_debug](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/debug/res/mipmap-xhdpi/ic_launcher.png)   |
| [Parc amazonien de Guyane](https://www.parc-amazonien-guyane.fr)     | _pag_     | ![PAG](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/pag/res/mipmap-xhdpi/ic_launcher.png) ![PAG_debug](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/pagDebug/res/mipmap-xhdpi/ic_launcher.png) |
| [Parc National des Cévennes](http://www.cevennes-parcnational.fr)    | _pnc_     | ![PNC](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/pnc/res/mipmap-xhdpi/ic_launcher.png) ![PNC_debug](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/pncDebug/res/mipmap-xhdpi/ic_launcher.png) |
| [Parc National des Écrins](http://www.ecrins-parcnational.fr)        | _pne_     | ![PNE](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/pne/res/mipmap-xhdpi/ic_launcher.png) ![PNE_debug](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/pneDebug/res/mipmap-xhdpi/ic_launcher.png) |
| [Parc National du Mercantour](http://www.mercantour-parcnational.fr) | _pnm_     | ![PNM](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/pnm/res/mipmap-xhdpi/ic_launcher.png) ![PNM_debug](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/pnmDebug/res/mipmap-xhdpi/ic_launcher.png) |
| [Parc National de la Vanoise](http://www.vanoise-parcnational.fr)    | _pnv_     | ![PNV](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/pnv/res/mipmap-xhdpi/ic_launcher.png) ![PNV_debug](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/pnvDebug/res/mipmap-xhdpi/ic_launcher.png) |

## Settings

Example:

```json
{
  "area_observation_duration": 365,
  "sync": {
    "geonature_url": "https://demo.geonature/geonature",
    "taxhub_url": "https://demo.geonature/taxhub",
    "uh_application_id": 3,
    "observers_list_id": 1,
    "taxa_list_id": 100,
    "code_area_type": "M1",
    "page_size": 10000
  },
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

| Parameter                   | UI      | Description                                                                                        | Default value |
| --------------------------- | ------- | -------------------------------------------------------------------------------------------------- | ------------- |
| `area_observation_duration` | &#9744; | Area observation duration period (in days)                                                         | 365           |
| `sync`                      | &#9744; | Data synchronization settings (cf. https://github.com/PnX-SI/gn_mobile_core/tree/develop/datasync) |               |
| `map`                       | &#9744; | Maps settings (cf. https://github.com/PnX-SI/gn_mobile_maps/tree/develop/maps)                     |               |
| `nomenclature`              | &#9744; | Nomenclature settings                                                                              |               |
| `nomenclature/information`  | &#9744; | Information settings (as array)                                                                    |               |
| `nomenclature/counting`     | &#9744; | Counting settings (as array)                                                                       |               |

#### Nomenclature settings

Allows to define if fields are displayed by default and if they are editable (visible). If a field is not editable (visible),
it will use the default value set in Occtax database.

All these settings may not be defined and the default values will then be used instead:

**Information settings**

| Nomenclature     | Label                | Displayed by default | Editable (visible) |
| ---------------- | -------------------- | -------------------- | ------------------ |
| METH_OBS         | Observation methods  | `true`               | `true`             |
| ETA_BIO          | Biological state     | `true`               | `true`             |
| METH_DETERMIN    | Determination method | `false`              | `true`             |
| DETERMINER       | Determiner           | `false`              | `true`             |
| STATUT_BIO       | Biological status    | `false`              | `true`             |
| OCC_COMPORTEMENT | Behaviour            | `false`              | `true`             |
| NATURALITE       | Level of naturalness | `false`              | `true`             |
| PREUVE_EXIST     | Proof of existence   | `false`              | `true`             |
| COMMENT          | Comment              | `false`              | `true`             |

**Counting settings**

| Nomenclature | Label                      | Displayed by default | Editable (visible) |
| ------------ | -------------------------- | -------------------- | ------------------ |
| STADE_VIE    | Life stage                 | `true`               | `true`             |
| SEXE         | Sex                        | `true`               | `true`             |
| OBJ_DENBR    | Purpose of the enumeration | `true`               | `true`             |
| TYP_DENBR    | Type of enumeration        | `true`               | `true`             |
| MIN          | Min                        | `true`               | `true`             |
| MAX          | Max                        | `true`               | `true`             |

**Note:** Any unknown nomenclature attribute added will be simply ignored at startup.

You can override these default settings by adding a property for each nomenclature settings, e.g:

```json
{
  "nomenclature": {
    "information": [
      "METH_OBS",
      {
        "key": "ETA_BIO"
      },
      {
        "key": "METH_DETERMIN",
        "visible": true,
        "default": true
      },
      {
        "key": "STATUT_BIO",
        "visible": true,
        "default": false
      },
      {
        "key": "OCC_COMPORTEMENT",
        "visible": true,
        "default": false
      },
      {
        "key": "NATURALITE",
        "visible": true,
        "default": false
      },
      {
        "key": "PREUVE_EXIST",
        "visible": true,
        "default": false
      }
    ],
    "counting": ["STADE_VIE", "SEXE", "OBJ_DENBR", "TYP_DENBR"]
  }
}
```

Each property may be a simple string representing the nomenclature attribute to show or an object with the following properties:

| Property  | Description                                                           | Mandatory |
| --------- | --------------------------------------------------------------------- | --------- |
| `key`     | The nomenclature attribute                                            | &#9745;   |
| `visible` | If this attribute is visible (thus editable) or not (default: `true`) | &#9744;   |
| `default` | If this attribute is shown by default (default: `true`)               | &#9744;   |

**Example:**

- `"METH_OBS"` has the same meaning like

  ```json
  {
    "key": "METH_OBS"
  }
  ```

  or

  ```json
  {
    "key": "METH_OBS",
    "visible": true
  }
  ```

  or

  ```json
  {
    "key": "METH_OBS",
    "default": true
  }
  ```

  or

  ```json
  {
    "key": "METH_OBS",
    "visible": true,
    "default": true
  }
  ```

- An omitted property (e.g. `METH_OBS`) has the same meaning like

  ```json
  {
    "key": "METH_OBS",
    "visible": false
  }
  ```

## Upgrade git sub modules

Do **NOT** modify directly any git sub modules (e.g. `commons`, `mountpoint`, `viewpager` and `maps`).
Any changes should be made from each underlying git repository:

- `commons`: [gn_mobile_core](https://github.com/PnX-SI/gn_mobile_core) git repository
- `datasync`: [gn_mobile_core](https://github.com/PnX-SI/gn_mobile_core) git repository
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
