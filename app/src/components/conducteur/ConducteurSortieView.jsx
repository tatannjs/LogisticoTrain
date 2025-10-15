import React, { useContext, useState } from 'react';
import PropTypes from 'prop-types';
// import classNames from 'classnames';
import { observer, PropTypes as MPropTypes } from 'mobx-react';
import {
  Alert, Button, Col, ListGroup, Modal, Row,
} from 'react-bootstrap';
import RootStore from '../../RootStore';

function IntRameSortieConfirmationModal({ rame, onConfirm, onCancel }) {
  const message = rame ? `Confirmez-vous le retrait de la rame ${rame.numSerie} ?` : '';
  return (
    <Modal show={rame} onHide={onCancel}>
      <Modal.Header closeButton>
        <Modal.Title>Confirmation de retrait</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        {message}
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={onCancel}>
          Annuler
        </Button>
        <Button variant="primary" onClick={onConfirm}>
          Confirmer
        </Button>
      </Modal.Footer>
    </Modal>
  );
}

IntRameSortieConfirmationModal.propTypes = {
  rame: MPropTypes.objectOrObservableObject.isRequired,
  onConfirm: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
};

const RameSortieConfirmationModal = observer(IntRameSortieConfirmationModal);

function ConducteurSortieView() {
  const { main: mainStore } = useContext(RootStore);
  const [rameToRemove, setRameToRemove] = useState(null);

  const selectRameToRemove = (rame) => {
    if (!!rameToRemove || mainStore.removing || mainStore.rameRemoved) {
      return;
    }
    setRameToRemove(rame);
  };

  const confirmRemoveRame = () => {
    if (!rameToRemove || mainStore.removing || mainStore.rameRemoved) {
      return;
    }
    const rame = rameToRemove;
    setRameToRemove(null);
    mainStore.removeRame(rame);
  };

  const cancelRemoveRame = () => {
    setRameToRemove(null);
  };

  return (
    <>
      {rameToRemove && (
      <RameSortieConfirmationModal
        rame={rameToRemove}
        onConfirm={confirmRemoveRame}
        onCancel={cancelRemoveRame}
      />
      )}
      <Row className="justify-content-center">
        <Col xs={12} md={6} xl={4}>
          <h5>Rames disponibles à la sortie</h5>
          <p className="fst-italic">Selectionnez la rame à sortir du dépot</p>
          <ListGroup className="mt-3">
            {mainStore.removableRames.length
              ? mainStore.removableRames.map((rame) => (
                <ListGroup.Item
                  key={rame.numSerie}
                  action
                  disabled={!!rameToRemove || mainStore.removing || mainStore.rameRemoved}
                  active={rame === rameToRemove}
                  onClick={() => selectRameToRemove(rame)}
                >
                  {`Voie ${rame.numVoie} : ${rame.numSerie} (${rame.typeRame})`}
                </ListGroup.Item>
              ))
              : (
                <ListGroup.Item variant="warning">
                  Aucune rame disponible au retrait
                </ListGroup.Item>
              )}
          </ListGroup>
        </Col>
      </Row>
      <Row className="justify-content-center mt-4">
        <Col xs={12} md={8} xl={6}>
          {mainStore.error && (
          <Alert variant="danger">
            <Alert.Heading>Erreur de traitement</Alert.Heading>
            <p>
              Une erreur est survenue :&nbsp;
              {mainStore.error?.message ?? 'erreur inconnue.'}
            </p>
          </Alert>
          )}
          {mainStore.rameRemoved && (
          <Alert variant="success">
            <Alert.Heading>Rame retirée</Alert.Heading>
            <p>
              Le retrait de la rame
              {' '}
              {mainStore.rameRemoved.numSerie}
              {' '}
              du dépôt a été enregistré.
            </p>
          </Alert>
          )}
        </Col>
      </Row>
    </>
  );
}

export default observer(ConducteurSortieView);
