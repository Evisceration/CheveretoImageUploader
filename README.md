CheveretoImageUploader
========
This is an example Android Application, which interacts with the Chevereto API.

With it, your Users can upload images, which they choose in the gallery or take them with their camera, to your Installation of Chevereto.

###How To use it?
Open "Constants.java" and replace "YOUR_API_KEY" with the API Key of your Chevereto Installation.

Also you may want to update the urls, so that the Updater works for you.


###How the Updating works
When updating, "URL" + "versionFile" of your "Constants.java" will get combined, and checked.
it should contain a SINGLE NUMBER!
it parses this number and checks it with the installed Version.

If the parsed number is higher than the installed Version, it will download "urls" + "fileName" + the parsed number + .apk
For example, the parsed number is 4381, it will then download "http://android.openfire-security.net/files/apps/image/image_4381.apk"


###Bugs
Currently when the images are too big, they may are not displayed in the preview and can crash the uploading progress.