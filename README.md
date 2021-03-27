# Script - Photos organizeFiles.OrganizerClass

[![Downloads](https://img.shields.io/github/downloads/MarciaBM/script-photos/total)]()
[![Languages](https://img.shields.io/github/languages/top/MarciaBM/script-photos)]()

This is a program made with **Java** to help you organize your messy folder of pictures and videos. Developed for the **Linux** and **MacOS** operating systems. You can run it on **Windows** but will only work for images, videos features will be disable.
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
For MacOS users:
If you don't have **brew** installed, install it by:
```
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)" 
```
To install Java:
```
brew install java11
```
```
echo 'export PATH="/usr/local/opt/openjdk@11/bin:$PATH"' >> ~/.profile
```
And then reboot the terminal.

Installation
------------------
. Start for cloning our repository, you will need to have **Git** installed (usually it comes already installed on Linux).
```
git clone https://github.com/MarciaBM/script-photos/
```
Or you can just click on the Code green button and download the .zip file.

. Open your terminal inside the downloaded folder at **script-photos/src** and run:
```
javac -cp metadata-extractor-2.15.0.jar:opencv-451.jar Main.java
```
```
java -cp .:metadata-extractor-2.15.0.jar:opencv-451.jar Main [path of the folder you want to run the script]
```
In Windows replace : for ;
And that's it, your running our script, hope you like it and enjoy it!

Credits: Pedro and Márcia
------------

<li><a href="https://paypal.me/pedrorxpgrilo" rel="nofollow"><img src="https://camo.githubusercontent.com/a18b4bf3a695fb7a3c6eff91238fe45862849a8b38ffe492764d33fc73036de2/68747470733a2f2f696f6e69636162697a61752e6769746875622e696f2f6261646765732f70617970616c2e737667" alt="paypal.me/pedrorxogrilo" data-canonical-src="https://ionicabizau.github.io/badges/paypal.svg" style="max-width:100%;"></a> - You can make one-time donations via PayPal. I'll probably buy a <del>coffee</del> tea. <g-emoji class="g-emoji" alias="tea" fallback-src="https://github.githubassets.com/images/icons/emoji/unicode/1f375.png">🍵</g-emoji></li>
