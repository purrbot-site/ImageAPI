# PurrBot Image API
This API was created to provide random images.  
The reason behind this and not to just use any existing api (e.g. nekos.life) was to have more controll over the shown images.

You can see the API being used by the bot [\*Purr*](https://purrbot.site).

## Endpoints
> **Base-URL**:  
> `https://purrbot.site/api/img`

Below is a list of all available endpoints.  
`:category` is the name of the category (e.g. neko) and `:type` is the image type (`img` for image and `gif` for gifs)

### SFW (`/sfw/:category/:type`)
Contains SFW (Safe for work) images and gifs.

| Category:    | Supported Types: |
| ------------ | ---------------- |
| `background` | `img`            |
| `cuddle`     | `gif`            |
| `holo`       | `img`            |
| `hug`        | `gif`            |
| `icon`       | `img`            |
| `kiss`       | `gif`            |
| `kitsune`    | `img`            |
| `lick`       | `gif`            |
| `neko`       | `gif`            |
|              | `img`            |
| `pat`        | `gif`            |
| `poke`       | `gif`            |
| `senko`      | `img`            |
| `slap`       | `gif`            |
| `tickle`     | `gif`            |

### NSFW (`/nsfw/:category/:type`)
Contains NSFW (Not safe for work) images and gifs.

| Category:       | Supported Types: |
| --------------- | ---------------- |
| `blowjob`       | `gif`            |
| `fuck`          | `gif`            |
| `neko`          | `gif`            |
|                 | `img`            |
| `solo`          | `gif`            |
| `threesome_fff` | `gif`            |
| `threesome_ffm` | `gif`            |
| `threesome_mmf` | `gif`            |
| `yuri`          | `gif`            |

## Report images
Please report any images that may be seen as illegal (i.e. against a Companies ToS) on [our Discord](https://purrbot.site/discord) or through mail at support@purrbot.site