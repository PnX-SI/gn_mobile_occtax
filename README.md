# Occtax-mobile

GeoNature Android mobile application for _Occtax_ GeoNature module.

Based on [datasync module](https://github.com/PnX-SI/gn_mobile_core) to synchronize data and
[maps module](https://github.com/PnX-SI/gn_mobile_maps) to show maps data.

## Documentation

- [Installation (in french)](./docs/installation-fr.adoc)
- [User documentation (in french)](./docs/utilisation-fr.adoc)
- [Data synchronization workflow](https://github.com/PnX-SI/gn_mobile_core/blob/develop/docs/data_sync.adoc)
- [Input workflow](./docs/input_workflow.adoc)
- [Customization](https://github.com/PnX-SI/gn_mobile_core/blob/develop/docs/styles_themes.adoc)

## Launcher icons

| Name    | Flavor    | Launcher icon                                                                                                                                                                                                                                                    |
| ------- | --------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Default | _generic_ | ![PNX](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/main/res/mipmap-xxxhdpi/ic_launcher.png) ![PNX_debug](https://raw.githubusercontent.com/PnX-SI/gn_mobile_occtax/develop/occtax/src/debug/res/mipmap-xxxhdpi/ic_launcher.png) |

## Settings

The app settings file is automatically updated locally when the application is started, as soon as the *GeoNature* URL is defined, with the one on *GeoNature* server.
This settings file `settings_occtax.json` can be found locally in the `Android/data/fr.geonature.occtax2/` directory of the terminal's main storage. Example:

```json
{
  "area_observation_duration": 365,
  "sync": {
    "geonature_url": "https://demo.geonature/geonature",
    "taxhub_url": "https://demo.geonature.fr/geonature/api/taxhub",
    "gn_application_id": 3,
    "observers_list_id": 1,
    "taxa_list_id": 100,
    "code_area_type": "M1",
    "page_size": 10000
  },
  "map": {
    "show_scale": true,
    "show_compass": true,
    "max_bounds": [
      [52.0, -6.0],
      [41.0, 9.0]
    ],
    "center": [46.0, 3.0],
    "start_zoom": 10.0,
    "min_zoom": 8.0,
    "max_zoom": 19.0,
    "min_zoom_editing": 12.0,
    "base_path": "Offline_maps",
    "layers": [
      {
        "label": "IGN: plan v2",
        "source": "https://wxs.ign.fr/essentiels/geoportail/wmts?REQUEST=GetTile&SERVICE=WMTS&VERSION=1.0.0&STYLE=normal&TILEMATRIXSET=PM&FORMAT=image/png&LAYER=GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2"
      },
      {
        "label": "IGN: ortho",
        "source": "https://wxs.ign.fr/ortho/geoportail/wmts?REQUEST=GetTile&SERVICE=WMTS&VERSION=1.0.0&STYLE=normal&TILEMATRIXSET=PM&FORMAT=image/jpeg&LAYER=ORTHOIMAGERY.ORTHOPHOTOS"
      },
      {
        "label": "OpenStreetMap",
        "source": [
          "https://a.tile.openstreetmap.org",
          "https://b.tile.openstreetmap.org",
          "https://c.tile.openstreetmap.org"
        ]
      },
      {
        "label": "OpenTopoMap",
        "source": [
          "https://a.tile.opentopomap.org",
          "https://b.tile.opentopomap.org",
          "https://c.tile.opentopomap.org"
        ]
      },
      {
        "label": "IGN plan",
        "source": "plan.mbtiles"
      },
      {
        "label": "IGN ortho",
        "source": "ortho.mbtiles"
      },
      {
        "label": "Mailles 5x5",
        "source": "mailles.geojson"
      }
    ]
  },
  "input": {
    "date": {
      "enable_end_date": true,
      "enable_hours": true
    }
  }
}
```

### Parameters description

| Parameter                          | UI      | Description                                                                                        | Default value |
|------------------------------------| ------- |----------------------------------------------------------------------------------------------------| ------------- |
| `area_observation_duration`        | &#9744; | Area observation duration period (in days)                                                         | 365           |
| `sync`                             | &#9744; | Data synchronization settings (cf. https://github.com/PnX-SI/gn_mobile_core/tree/develop/datasync) |               |
| `map`                              | &#9744; | Maps settings (cf. https://github.com/PnX-SI/gn_mobile_maps/tree/develop/maps)                     |               |
| `input`                            | &#9744; | Input form settings                                                                                |               |
| `input/date`                       | &#9744; | Date settings                                                                                      |               |
| `nomenclature`                     | &#9744; | Nomenclature settings                                                                              |               |
| `nomenclature/save_default_values` | &#9744; | Save default nomenclature values                                                                   | false         |
| `nomenclature/additional_fields`   | &#9744; | Show additional fields                                                                             | false         |
| `nomenclature/information`         | &#9744; | Information settings (as array)                                                                    |               |
| `nomenclature/counting`            | &#9744; | Counting settings (as array)                                                                       |               |

### Input settings

Allows to configure settings related to user input.

**Date settings**

How the user can set the start and end date of the input:

| Parameter         | Description                                                                  | Default value |
| ----------------- | ---------------------------------------------------------------------------- | ------------- |
| `enable_end_date` | Whether to edit as well the end date of the input                            | `false`       |
| `enable_hours`    | Whether to edit as well the hour part of the start and end date (if enabled) | `false`       |

The combination of these parameters gives the following configuration:

- `enable_end_date` and `enable_hours` are set to `false`: only the start date without the hour part is editable
- only `enable_end_date` is set to `true`: the start and end date without the hour part are editable
- only `enable_hours` is set to `true`: only the start date including the hour part is editable
- `enable_end_date` and `enable_hours` are set to `true`: the start and end date with the hour part are editable

If nothing is configured, only the start date without the hour part is editable.

### Nomenclature settings

`save_default_values`: Allows to save locally and only during a session of use selected nomenclature
values as default values (default: `false`).

Allows to define if fields are displayed by default and if they are editable (visible).
If a field is not editable (visible), it will use the default value set in Occtax database.

All these settings may not be defined and the default values will then be used instead:

**Information settings**

| Nomenclature       | Label                | Displayed by default | Editable (visible) |
| ------------------ | -------------------- | -------------------- | ------------------ |
| `METH_OBS`         | Observation methods  | `true`               | `true`             |
| `ETA_BIO`          | Biological state     | `true`               | `true`             |
| `METH_DETERMIN`    | Determination method | `false`              | `true`             |
| `determiner`       | Determiner           | `false`              | `true`             |
| `STATUT_BIO`       | Biological status    | `false`              | `true`             |
| `OCC_COMPORTEMENT` | Behaviour            | `false`              | `true`             |
| `NATURALITE`       | Level of naturalness | `false`              | `true`             |
| `PREUVE_EXIST`     | Proof of existence   | `false`              | `true`             |
| `comment`          | Comment              | `false`              | `true`             |

**Counting settings**

| Nomenclature | Label                      | Displayed by default | Editable (visible) |
| ------------ | -------------------------- | -------------------- | ------------------ |
| `STADE_VIE`  | Life stage                 | `true`               | `true`             |
| `SEXE`       | Sex                        | `true`               | `true`             |
| `OBJ_DENBR`  | Purpose of the enumeration | `true`               | `true`             |
| `TYP_DENBR`  | Type of enumeration        | `true`               | `true`             |
| `count_min`  | Min                        | `true`               | `true`             |
| `count_max`  | Max                        | `true`               | `true`             |
| `medias`     | Medias                     | `true`               | `true`             |

**Note:** Any unknown nomenclature attribute added will be simply ignored at startup.

You can override these default settings by adding a property for each nomenclature settings, e.g:

```json
{
  "nomenclature": {
    "save_default_values": false,
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

### Override parameters from app settings

As this local settings file is updated automatically with the one on *GeoNature* server, we advise you not to update it directly by hand.
To do this, you can locally overwrite the values from the *GeoNature* server by creating a `settings_occtax.local.json` file in the same location as the app settings file in the terminal's main storage.
Then simply copy the parameters to be overwritten, respecting the JSON structure of the app settings file. Example, to override map layers configuration:

```json
{
  "map": {
    "layers": [
      {
        "label": "OpenStreetMap",
        "source": [
          "https://a.tile.openstreetmap.org",
          "https://b.tile.openstreetmap.org",
          "https://c.tile.openstreetmap.org"
        ]
      },
      {
        "label": "My awesome layer",
        "source": "custom.mbtiles"
      }
    ]
  }
}
```

**⚠ Note:** When using such a configuration, which can potentially replace all the application's parameters, it's your responsibility to ensure that the final configuration is always valid and keeps pace with any changes made to *GeoNature*.

In case of errors, if the final configuration is incorrect, the application will automatically ignore the local settings file that allows you to replace the parameters and load the default configuration from *GeoNature*. Any errors reported can be found in the application logs.

## Upgrade git sub modules

Do **NOT** modify directly any git sub modules (e.g. `commons`, `compat`, `mountpoint`, `viewpager`
and `maps`). Any changes should be made from each underlying git repository:

- `commons`: [gn_mobile_core](https://github.com/PnX-SI/gn_mobile_core) git repository
- `compat`: [gn_mobile_core](https://github.com/PnX-SI/gn_mobile_core) git repository
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

## Financial support

This application have been developed with the financial support of the [Office Français de la Biodiversité](https://www.ofb.gouv.fr).
