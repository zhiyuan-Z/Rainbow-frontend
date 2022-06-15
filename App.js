import React from 'react';
import * as eva from '@eva-design/eva';
import { ApplicationProvider, IconRegistry } from '@ui-kitten/components';
import HomeScreen from './src/pages/HomeScreen';
import { EvaIconsPack } from '@ui-kitten/eva-icons';

const App = () => {

  return (
    <>
      <IconRegistry icons={EvaIconsPack} />
      <ApplicationProvider {...eva} theme={eva.light}>
        <HomeScreen />
      </ApplicationProvider>
    </>
  );
};

export default App;
