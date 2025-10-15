import React, { useContext, useState } from 'react';
// import PropTypes from 'prop-types';
import classNames from 'classnames';
import {
  Button, Col, Form, Row,
} from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { observer } from 'mobx-react';
import RootStore from '../RootStore';
import User from '../model/User';

function Landing() {
  const { user } = useContext(RootStore);
  const [formState, setFormState] = useState({
    username: '',
    role: '',
    validated: false,
  });
  const navigate = useNavigate();

  const submitUsername = (evt) => {
    evt.preventDefault();
    const form = evt.currentTarget;
    if (form.checkValidity() === false) {
      evt.stopPropagation();
    } else {
      const cleanedUsername = formState.username.trim();
      if (!cleanedUsername || !formState.role) {
        setFormState((state) => ({
          ...state,
          username: '',
          role: '',
        }));
      } else {
        user.authenticate(cleanedUsername, formState.role);
        navigate('/');
      }
    }
    setFormState((state) => ({
      ...state,
      validated: true,
    }));
  };

  const setUsername = (username) => setFormState((state) => ({
    ...state,
    validated: false,
    username,
  }));

  const setRole = (role) => setFormState((state) => ({
    ...state,
    validated: false,
    role,
  }));

  return (
    <Row className={classNames('align-items-center', 'justify-content-center', 'vh-full')}>
      <Col xs={12} md={6} xl={4}>
        <Form noValidate validated={formState.validated} onSubmit={submitUsername}>
          <Form.Group className="mb-3" controlId="landing.username">
            <Form.Label>Nom d&lsquo;utilisateur</Form.Label>
            <Form.Control
              type="text"
              size="lg"
              placeholder="Votre nom d'opérateur"
              minLength={2}
              maxLength={50}
              required
              value={formState.username}
              onChange={(e) => setUsername(e.target.value)}
            />
          </Form.Group>
          <Form.Group className="mb-3" controlId="landing.role">
            <Form.Label>Role</Form.Label>
            <div className="mb-3">
              {
                User.roles.map(({ name, description }) => (
                  <Form.Check
                    key={name}
                    inline
                    label={description}
                    name="role"
                    type="radio"
                    id={`landing.role.${name}`}
                    required
                    value={name}
                    onChange={(e) => setRole(e.target.value)}
                  />
                ))
              }
            </div>
          </Form.Group>

          <Button type="submit" variant="success" size="lg">Valider</Button>
        </Form>
      </Col>
    </Row>
  );
}

export default observer(Landing);
