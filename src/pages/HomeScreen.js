import React, { useState } from 'react';
import * as eva from '@eva-design/eva';
import { Icon, Button, Layout, Text } from '@ui-kitten/components';
import { StyleSheet, Image } from 'react-native';
import launchGallery from '../utils/launchImageLibrary'

const ImageIcon = (props) => (
  <Icon {...props} name='image'/>
);

const HomeScreen = () => {
  const [image, setImage] = useState(null);
  
  const updateImage = (props) => {
    setImage(props);
  }

  return (
    <>
      <Layout style={{flex: 1, justifyContent: 'center', alignItems: 'center'}}>
        <Text style={styles.name} category='h1'>Rainbow</Text>
        {image && <Image source={{ uri: image.filePath.uri }} style={{ width: 300, height: 400 }} />}
        {image && <Text category='h5'>{image.uri}</Text>}
        {image && console.log(image)}
        <Button style={{margin: 2, height: 150, width: 150}} onPress={() => launchGallery(updateImage)} appearance='outline' status='basic' accessoryLeft={ImageIcon} />
      </Layout>
    </>
  )
};

const styles = StyleSheet.create({
  name: {
    color: '#4169e1',
    marginBottom: 50
  },
  icon: {
    width: 32,
    height: 32,
  },
});

export default HomeScreen;