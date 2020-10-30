[docs]: https://docs.purrbot.site/api/imageapi
[purr]: https://purrbot.site

# PurrBot Image API
This API was created to provide random images.  
The reason behind this and not to just use any existing api (e.g. nekos.life) was to have more controll over the shown images.

With version 1.2.0 was this API now merged with the original PurrBotAPI to have one central API to use.

You can see the API being used by the bot [\*Purr*][purr].

**This page only lists the endpoints! Please read the [documentation][docs] for more detailed info.**

## Endpoints
> **Base-URL**:  
> `https://purrbot.site/api`

### `/quote`
> **Type**: POST  
> **Description**: Generates images that look like Discord messages.

| Field:     | Type:  | Description:                                                                | Default:                                        |
| ---------- | ------ | --------------------------------------------------------------------------- | ----------------------------------------------- |
| avatar     | String | The URL of the avatar to display.                                           | https://purrbot.site/assets/img/api/unknown.png |
| dateFormat | String | Format for the date. Can be f.e. `MM/dd/yyyy` or `dd. MM. yyyy`.            | `dd. MMM yyyy hh:mm:ss zzz`                     |
| message    | String | The message of the user.                                                    | `Some message`                                  |
| nameColor  | String | Color of name. Requires either `hex:rrggbb`, `rgb:r,g,b` or an inter value. | `#ffffff`                                       |
| timestamp  | Number | The time as echo time millis.                                               | `<Current time of request>`                     |
| username   | String | Name of the user.                                                           | `Someone`                                       |

----
### `/status`
> **Type**: POST  
> **Description**: Adds a status indicator to the provided Avatar.

| Field: | Type:   | Description:                                                                                                                   | Default:                                        |
| ------ | ------- | ------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------- |
| avatar | String  | The URL of the avatar to display.                                                                                              | https://purrbot.site/assets/img/api/unknown.png |
| mobile | Boolean | If the user is on mobile. `true` will change the icon to the one used in Discord to indicate a mobile user (Small smartphone). | `false`                                         |
| status | String  | The status to set as icon. Can be `online`, `idle`, `do_not_disturb` (or `dnd`), `streaming` or `offline`                      | `offline`                                       |

----
### SFW (`/img/sfw/:category/:type`)
> **Type**: GET  
> **Description**: Contains SFW (Safe for work) images and gifs.

Replace `:category` with a category name and `:type` with the category's supported type.

| Category:    | Types: |
| ------------ | ------ |
| `background` | `img`  |
| `bite`       | `gif`  |
| `blush`      | `gif`  |
| `cuddle`     | `gif`  |
| `eevee`      | `gif`  |
|              | `img`  |
| `feed`       | `gif`  |
| `fluff`      | `gif`  |
| `holo`       | `img`  |
| `hug`        | `gif`  |
| `icon`       | `img`  |
| `kiss`       | `gif`  |
| `kitsune`    | `img`  |
| `lick`       | `gif`  |
| `neko`       | `gif`  |
|              | `img`  |
| `pat`        | `gif`  |
| `poke`       | `gif`  |
| `senko`      | `img`  |
| `slap`       | `gif`  |
| `tail`       | `gif`  |
| `tickle`     | `gif`  |

----
### NSFW (`/img/nsfw/:category/:type`)
> **Type**: GET  
> **Description**: Contains NSFW (Not Safe for work) images and gifs.

Replace `:category` with a category name and `:type` with the category's supported type.

| Category:       | Types: |
| --------------- | ------ |
| `anal`          | `gif`  |
| `blowjob`       | `gif`  |
| `cum`           | `gif`  |
| `fuck`          | `gif`  |
| `neko`          | `gif`  |
|                 | `img`  |
| `pussylick`     | `gif`  |
| `solo`          | `gif`  |
| `threesome_fff` | `gif`  |
| `threesome_ffm` | `gif`  |
| `threesome_mmf` | `gif`  |
| `yaoi`          | `gif`  |
| `yuri`          | `gif`  |

## Report images
Please report any images that may be seen as illegal (i.e. against a company ToS) on [our Discord](https://purrbot.site/discord) or through mail at support@purrbot.site
