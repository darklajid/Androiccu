Androiccu IPv6 Tunnels
======================

A GUI to install, configure and run IPv6 tunnels using aiccu and tunnels provided by sixxs.net

You must have root on your phone to be able to install and run aiccu.
You must have the tun.ko kernel module installed or this feature must be built into the kernel. Most available firmware images already provide this. Otherwise use the tun.ko application available in Google play (https://play.google.com/store/apps/details?id=com.aed.tun.installer).
You must have busybox installed, most alternative firmware do. Otherwise install from Google play.

This application is in beta state, I can't be held responsible for anything not working or bad things done to your phone.

Plans:
[ ] remove or lessen dependency on busybox
[ ] auto start on boot
[ ] auto disable while native IPv6 available
[ ] reestablish 'auto-reconnect' on network change, while preventing hammering sixxs.net's server and be blacklisted
[ ] have a real application logo

Have fun testing and provide feedback if it doesn't work out yet for you !

If in the config menu it doesn't list your tunnel when searching for tunnels, make sure your tunnel is configured as 'AYIYA' type tunnel (has to be configured on your sixxs.net account).

The latest release of the app is found in Google's PlayStore at https://play.google.com/store/apps/details?id=ch.web_troubles.androiccu

Developping
===========

This application needs the android.support.v7.gridlayout library provided with your Android SDK, so add it to the project ;-)
