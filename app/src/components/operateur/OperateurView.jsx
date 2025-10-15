import React, { useContext, useState } from 'react';
// import PropTypes from 'prop-types';
// import classNames from 'classnames';
import {
  ListGroup,
  Row,
  Col,
  Form,
  Button,
  Alert,
} from 'react-bootstrap';
import { observer } from 'mobx-react';
import RootStore from '../../RootStore';
import RameEntranceRequestCard from './RameEntranceRequestCard';
import VoieListItem from '../common/VoieListItem';
import TachesViewList from '../common/TachesViewList';
import Waiting from '../common/Waiting';
import HistoryActionsViewList from '../common/HistoryActionsViewList';

function OperateurView() {
  const { main: mainStore } = useContext(RootStore);
  const [newVoieState, setNewVoieState] = useState({
    numVoie: '', numVoieValid: null, creating: false, creationError: null,
  });

  const handleCreateVoie = (evt) => {
    evt.preventDefault();
    if (newVoieState.creating) {
      return;
    }
    const form = evt.currentTarget;
    const formValid = form.checkValidity() !== false;
    const numVoie = parseInt(newVoieState.numVoie, 10);
    const numVoieValid = !Number.isNaN(numVoie)
    && !mainStore.voies.some((v) => v.numVoie === numVoie);
    if (!formValid || !numVoieValid) {
      evt.stopPropagation();
      setNewVoieState((state) => ({
        ...state,
        formValidated: true,
        numVoieValid: false,
      }));
    } else {
      setNewVoieState((state) => ({
        ...state,
        formValidated: true,
        numVoieValid: true,
        creating: true,
        creationError: null,
      }));
      mainStore.createNewVoie(numVoie).then(
        () => setNewVoieState((state) => ({
          ...state, numVoie: '', creating: false, creationError: null,
        })),
        (error) => setNewVoieState((state) => ({
          ...state,
          creating: false,
          creationError: error,
        })),
      );
    }
  };

  return (
    <Waiting
      waiting={mainStore.rtConnecting && mainStore.loadingData ? 'Connexion en cours' : null}
      error={mainStore.rtError ? mainStore.rtError.toString() : null}
    >
      <Row>
        <Col xs={12} md={6} xl={4}>
          <h5>Demande d&lsquo;entrée</h5>
          {mainStore.rameEntranceRequests.map((request) => (
            <RameEntranceRequestCard
              key={request.numSerie}
              rameRequest={request}
              numVoies={mainStore.numVoiesDisponibles}
              onReject={() => mainStore.rejectRameEntranceRequest(request)}
              onAccept={(numVoie) => mainStore.acceptRameEntranceRequest(request, numVoie)}
            />
          ))}
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
        </Col>
        <Col xs={12} md={6} xl={4}>
          <h5>Etat du dépot</h5>
          <ListGroup>
            {
            mainStore.voies.map((voie) => (
              <VoieListItem
                key={voie.numVoie}
                voie={voie}
                rame={voie.rame}
                onSelect={voie.rame ? () => mainStore.selectRame(voie.rame) : null}
                onDelete={!voie.rame ? () => mainStore.deleteVoie(voie) : null}
                onSwitchInterdite={!voie.rame ? () => mainStore.switchVoieInterdite(voie) : null}
              />
            ))
          }
          </ListGroup>
          <Form className="mt-3" onSubmit={handleCreateVoie} noValidate>
            <fieldset disabled={newVoieState.creating}>
              <Row className="mb-1">

                <Form.Group as={Col} xs={12} className="mb-3 position-relative" controlId="rameEntranceRequest.typeRame">
                  <Form.Label>Type de rame</Form.Label>
                  <Form.Control
                    type="number"
                    placeholder="5"
                    min={1}
                    required
                    value={newVoieState.numVoie}
                    onChange={(evt) => setNewVoieState((state) => ({
                      ...state,
                      numVoie: evt.target.value,
                      numVoieValid: null,
                    }))}
                    isValid={newVoieState.numVoieValid === true}
                    isInvalid={newVoieState.numVoieValid === false}
                  />
                  <Form.Control.Feedback type="invalid" tooltip>Numéro de voie invalide ou déjà utilisé.</Form.Control.Feedback>
                </Form.Group>
              </Row>
              <Row className="justify-content-end">
                <Col xs="auto">
                  <Button variant="primary" type="submit">Ajouter une rame</Button>
                </Col>
              </Row>
              {newVoieState.creationError && (
              <Row className="mt-2">
                <Alert variant="danger">
                  <Alert.Heading>Erreur de création</Alert.Heading>
                  <p>{newVoieState.creationError?.message ?? 'Erreur inconnue'}</p>
                </Alert>
              </Row>
              )}
            </fieldset>
          </Form>

        </Col>
        <Col xs={12} md={6} xl={4}>
          {mainStore.selectedRame && (
          <div className="position-sticky">
            <Waiting
              waiting={mainStore.selectedRame.isloading ? 'Chargement en cours' : null}
              error={mainStore.selectedRame.loadingError
                ? mainStore.selectedRame.loadingError.toString() : null}
            >
              <h5>Tâches de la rame</h5>
              <TachesViewList taches={mainStore.selectedRame.taches} />
              <h5 className="mt-5">Historique des actions</h5>
              <HistoryActionsViewList actions={mainStore.selectedRame.actions} />
            </Waiting>
          </div>
          )}
        </Col>
      </Row>
    </Waiting>
  );
}

export default observer(OperateurView);
