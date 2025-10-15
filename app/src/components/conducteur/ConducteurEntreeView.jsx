import React, { useContext, useState } from 'react';
// import PropTypes from 'prop-types';
// import classNames from 'classnames';
import {
  Alert, Button, InputGroup, ListGroup, Form,
  Row,
  Col,
} from 'react-bootstrap';
import { observer } from 'mobx-react';
import RootStore from '../../RootStore';
import Waiting from '../common/Waiting';

function IntConducteurView() {
  const { main: mainStore } = useContext(RootStore);
  const [formValidated, setFormValidated] = useState(false);

  const handleSendRequest = (evt) => {
    evt.preventDefault();
    const form = evt.currentTarget;
    if (form.checkValidity() === false) {
      evt.stopPropagation();
    } else {
      mainStore.sendRequest();
    }
    setFormValidated(true);
  };

  // Vue mise en place connexion temps réel
  // Formulaire d'ajout de Rame
  // attente et blocage de réponse
  // réponse
  return (
    <Waiting
      waiting={mainStore.rtConnecting ? 'Connexion en cours' : null}
      error={mainStore.rtError ? mainStore.rtError.toString() : null}
    >
      <Form noValidate validated={formValidated} onSubmit={handleSendRequest} className="mb-4">
        <fieldset disabled={mainStore.waitingForAnswer || !mainStore.rtConnected}>
          <Form.Group className="mb-3" controlId="rameEntranceRequest.numSerie">
            <Form.Label>Numéro de série de rame</Form.Label>
            <Form.Control
              type="text"
              placeholder="s3456"
              required
              pattern="[a-zA-Z0-9]{1,12}"
              value={mainStore.numSerie}
              onChange={(evt) => { mainStore.numSerie = evt.target.value; }}
            />
          </Form.Group>
          <Form.Group className="mb-3" controlId="rameEntranceRequest.typeRame">
            <Form.Label>Type de rame</Form.Label>
            <Form.Control
              type="text"
              placeholder="72500"
              required
              value={mainStore.typeRame}
              onChange={(evt) => { mainStore.typeRame = evt.target.value; }}
            />
          </Form.Group>
          <Form.Group className="mb-3" controlId="rameEntranceRequest.typeRame">
            <Form.Label>Tâches à réaliser</Form.Label>
            <ListGroup>
              {
                mainStore.taches.length
                  ? mainStore.taches.map((tache, idx) => (
                    /* eslint-disable-next-line react/no-array-index-key */
                    <ListGroup.Item key={idx}>
                      <InputGroup>
                        <Form.Control
                          as="textarea"
                          aria-label="Une tâche"
                          rows={2}
                          required
                          value={tache}
                          onChange={(evt) => mainStore.replaceTache(evt.target.value, idx)}
                        />
                        <Button
                          variant="outline-danger"
                          type="button"
                          onClick={() => mainStore.removeTache(idx)}
                        >
                          X
                        </Button>
                      </InputGroup>
                    </ListGroup.Item>
                  ))
                  : (
                    <ListGroup.Item>
                      <Form.Control
                        type="text"
                        placeholder="aucune tache"
                        required
                        disabled
                        isInvalid={formValidated}
                      />
                    </ListGroup.Item>
                  )
          }
              <ListGroup.Item type="button" action onClick={() => mainStore.addNewTache()}>+</ListGroup.Item>
            </ListGroup>
          </Form.Group>
          <Button variant="success" type="submit">Demander une entrée au dépôt</Button>
        </fieldset>
      </Form>
      {
      mainStore.waitingForAnswer && (
        <Alert variant="primary">En attente de réponse</Alert>
      )
      }
      {
        mainStore.answer && mainStore.answer.accept && (
          <Alert variant="success">
            <Alert.Heading>Demande d&lsquo;entrée acceptée</Alert.Heading>
            <p>
              Votre demande a été acceptée. Veuillez vous garer à la voie n°
              {mainStore.answer.voie}
            </p>
          </Alert>
        )
      }
      {
        mainStore.answer && !mainStore.answer.accept && (
          <Alert variant="danger">
            <Alert.Heading>Demande d&lsquo;entrée rejetée</Alert.Heading>
            <p>
              Votre demande a été rejetée.
              Veuillez vous garer temporairement sur une voie de parking.
            </p>
            <hr />
            <p className="mb-0">
              <Alert.Link onClick={() => mainStore.reset()}>
                Proposer une nouvelle entrée
              </Alert.Link>
            </p>
          </Alert>
        )
      }
      {mainStore.error && (
      <Alert variant="danger">
        <Alert.Heading>Erreur de requête</Alert.Heading>
        <p>
          {mainStore.error.error ?? 'Erreur inconnue'}
              &nbsp;:&nbsp;
          {mainStore.error.message ?? 'Aucun détails fourni sur cette erreur'}
        </p>
      </Alert>
      )}
    </Waiting>
  );
}

const ObsIntConducteurView = observer(IntConducteurView);

function ConducteurEntreeView() {
  return (
    <Row className="justify-content-center">
      <Col xs={12} md={6} xl={4}>
        <ObsIntConducteurView />
      </Col>
    </Row>
  );
}

export default observer(ConducteurEntreeView);
