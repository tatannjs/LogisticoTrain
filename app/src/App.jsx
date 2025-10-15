import React from 'react';
import { RouterProvider, createBrowserRouter, redirect } from 'react-router-dom';
// import PropTypes from 'prop-types';
// import classNames from 'classnames';
import Root from './components/Root';
import FatalError from './components/FatalError';
import RootStore from './RootStore';
import STORE from './store';

import './App.scss';
import Landing from './components/Landing';
import ConducteurEntreeView from './components/conducteur/ConducteurEntreeView';
import ConducteurEntreeStore from './model/ConducteurEntreeStore';
import OperateurView from './components/operateur/OperateurView';
import OperateurStore from './model/OperateurStore';
import ConducteurLanding from './components/conducteur/ConducteurLanding';
import ConducteurSortieView from './components/conducteur/ConducteurSortieView';
import ConducteurSortieStore from './model/ConducteurSortieStore';
import TechnicienStore from './model/TechnicienStore';
import TechnicienView from './components/technicien/TechnicienView';

const router = createBrowserRouter([
  {
    path: '/',
    element: <Root />,
    errorElement: <FatalError />,
    children: [
      {
        index: true,
        element: <Landing />,
        loader: async () => {
          const { user } = STORE;
          await user.ready;
          if (user.authenticated) {
            throw redirect(`/${user.role}`);
          }
          return STORE.resetMain();
        },
      },
      {
        path: 'operateur',
        element: <OperateurView />,
        loader: async () => {
          const { user } = STORE;
          await user.ready;
          if (!user.isOperateur) {
            throw redirect('/');
          }
          return STORE.switchMain(OperateurStore);
        },
      },
      {
        path: 'technicien',
        element: <TechnicienView />,
        loader: async () => {
          const { user } = STORE;
          await user.ready;
          if (!user.isTechnicien) {
            throw redirect('/');
          }
          return STORE.switchMain(TechnicienStore);
        },
      },
      {
        path: 'conducteur',
        children: [
          {
            index: true,
            element: <ConducteurLanding />,
            loader: async () => {
              const { user } = STORE;
              await user.ready;
              if (!user.isConducteur) {
                throw redirect('/');
              }
              return STORE.resetMain();
            },
          },
          {
            path: 'entree',
            element: <ConducteurEntreeView />,
            loader: async () => {
              const { user } = STORE;
              await user.ready;
              if (!user.isConducteur) {
                throw redirect('/');
              }
              return STORE.switchMain(ConducteurEntreeStore);
            },
          },
          {
            path: 'sortie',
            element: <ConducteurSortieView />,
            loader: async () => {
              const { user } = STORE;
              await user.ready;
              if (!user.isConducteur) {
                throw redirect('/');
              }
              return STORE.switchMain(ConducteurSortieStore);
            },
          },
        ],
      },
    ],
  },
]);

export default function App() {
  return (
    <RootStore.Provider value={STORE}>
      <RouterProvider router={router} />
    </RootStore.Provider>
  );
}
