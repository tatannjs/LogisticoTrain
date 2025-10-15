import React, { useContext } from 'react';
import Navbar from 'react-bootstrap/Navbar';
import Nav from 'react-bootstrap/Nav';
import { NavLink, Link, useNavigate } from 'react-router-dom';
import { observer } from 'mobx-react';
import RootStore from '../RootStore';

import logoPict from '../assets/logo.png';

function AppNavbar() {
  const { user } = useContext(RootStore);
  const navigate = useNavigate();

  const disconnect = () => {
    user.desauthenticate();
    navigate('/');
  };

  return (
    <Navbar expand="xxl" fixed="top" bg="dark" variant="dark" className="py-1">
      <Navbar.Brand as={Link} to="/">
        <img
          src={logoPict}
          width="30"
          height="30"
          className="d-inline-block align-top"
          alt={`${APP_ENV_APP_TITLE} Logo`}
        />
        {' '}
        {APP_ENV_APP_TITLE}
        {
          user.authenticated && ` (${user.username})`
        }
      </Navbar.Brand>
      <Navbar.Toggle aria-controls="AppNavbar" />
      <Navbar.Collapse id="AppNavbar">
        {
          user.authenticated && (
            <Nav className="ms-auto">
              {user.isOperateur && (
                <Nav.Link as={NavLink} to="/operateur">Contrôle du centre</Nav.Link>
              )}
              {user.isTechnicien && (
                <Nav.Link as={NavLink} to="/technicien">Gestion des tâches</Nav.Link>
              )}
              {user.isConducteur && (
                <>
                  <Nav.Link as={NavLink} to="/conducteur/entree">Entree de rame</Nav.Link>
                  <Nav.Link as={NavLink} to="/conducteur/sortie">Sortie de rame</Nav.Link>
                </>
              )}
              <Nav.Link className="ms-xxl-5" onClick={disconnect}>Se déconnecter</Nav.Link>
            </Nav>
          )
        }
      </Navbar.Collapse>
    </Navbar>
  );
}

export default observer(AppNavbar);
