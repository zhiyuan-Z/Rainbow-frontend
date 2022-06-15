import {launchCamera, launchImageLibrary} from 'react-native-image-picker';

export default launchGallery = (updateImage) => {
  let options = {
    storageOptions: {
      skipBackup: true,
      path: 'images',
    },
  };

  launchImageLibrary(options, (response) => {
    console.log('Response = ', response);

    if (response.didCancel) {
      console.log('User cancelled image picker');
    } else if (response.error) {
      console.log('ImagePicker Error: ', response.error);
    } else if (response.customButton) {
      console.log('User tapped custom button: ', response.customButton);
      alert(response.customButton);
    } else {
      const source = { uri: response.uri };
      console.log('response', JSON.stringify(response));
      updateImage({
        filePath: response.assets[0],
      });
    }
  });
}