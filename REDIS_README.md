# REDIS

## Info
This file contains HOWTO instructions for installing redis on your environment

## Requirements
- Ubuntu 20.04
- User with shell and root rights on the system

*All instructions tested on Ubuntu 20, but they will work for other Debian Like system.*
*You can choose which you are familiar with.*

------------
## Installation
- Apdate repos
  ```sudo apt update```
- Install **redis-server** package
  ```sudo apt install redis-server```

## Configuration
Main config file is : **/etc/redis/redis.conf**
For configuration you mast use text editor - for example nano

- For Runing Redis at system unit - find and change **supervised** parameter to ```supervised systemd```
- Enabling autostart after reboot
  ```sudo systemctl enable redis.service```
- ReStart **redis** service
  ```sudo systemctl restart redis.service```
- Check Status
  ```sudo systemctl status redis```


## Work
For working with redis you can use **redis-cli** util
```redis-cli```

## Links
For more information you can read [official documentation](https://redis.io/documentation "official documentation") or [this short HOWTO](https://www.digitalocean.com/community/tutorials/how-to-install-and-secure-redis-on-ubuntu-20-04 "this short HOWTO")


