# Script - Photos Organizer

[![Downloads](https://img.shields.io/github/downloads/MarciaBM/script-photos/total.svg?maxAge=2592000?style=flat-square)](https://github.com/MarciaBM/script-photos/releases)

This is a program made with **Java** to help you organize your messy folder of pictures and videos. Initially developed for the **Linux** operating system.
It will automatically delete empty sub-folders and will recursevily detect if you have duplicated photos. Then you will be able to choose wich ones you want to keep and wich ones you don't. Also you will be able to choose the percentage of equality of the photos you want the program to detect. About the videos, for now it will only detect duplicated videos with 100% match of equality.

Later, we intend that the software will be able to organize your photos by year, using the date the photo was taken, if available.

Pre-Requisites
------------------

. You will need to be running a Linux operating system.

. You will need to have **Java SDK** installed.
For Ubuntu distros users:
```
sudo apt install openjdk-11-jdk
```

. You will need to have `openimageio-tools` library installed.
For Ubuntu distros users:
```
sudo apt install openimageio-tools
```

Installation
------------------
. Start for cloning our repository, you will need to have **Git** installed (usually it comes already installed on Linux).
```
git clone https://github.com/MarciaBM/script-photos/
```
Or you can just click on the Code green button and download the .zip file.

. Open your terminal inside the downloaded folder at **script-photos/src** and run:
```
javac Main.java
```
```
java Main [path of the folder you want to run the script]
```

And that's it, your running our script, hope you like it and enjoy it!

Credits: Pedro and Márcia
------------

<li><a href="https://paypal.me/pedrorxpgrilo" rel="nofollow"><img src="https://camo.githubusercontent.com/a18b4bf3a695fb7a3c6eff91238fe45862849a8b38ffe492764d33fc73036de2/68747470733a2f2f696f6e69636162697a61752e6769746875622e696f2f6261646765732f70617970616c2e737667" alt="paypal.me/pedrorxogrilo" data-canonical-src="https://ionicabizau.github.io/badges/paypal.svg" style="max-width:100%;"></a> - You can make one-time donations via PayPal. I'll probably buy a <del>coffee</del> tea. <g-emoji class="g-emoji" alias="tea" fallback-src="https://github.githubassets.com/images/icons/emoji/unicode/1f375.png">🍵</g-emoji></li>
