import React from 'react';
import PropTypes from 'prop-types';
// import classNames from 'classnames';
import { observer, PropTypes as MPropTypes } from 'mobx-react';
import { Button, ButtonGroup, ListGroup } from 'react-bootstrap';

function VoieListItem({
  voie, rame, onSelect, onDelete, onSwitchInterdite,
}) {
  let itemVariant = 'success';
  if (voie.interdite) {
    itemVariant = 'danger';
  } else if (rame) {
    itemVariant = 'warning';
  }

  return (
    <ListGroup.Item
      as="li"
      className="d-flex justify-content-between align-items-start"
      variant={itemVariant}
    >
      {
        rame ? (
          <>
            <div className="ms-2 me-auto">
              <div className="fw-bold">
                {`Voie ${voie.numVoie} : ${rame.numSerie} (${rame.typeRame})`}
              </div>
            </div>
            <Button size="sm" disabled={!onSelect} onClick={onSelect} variant="info">Details</Button>
          </>
        ) : (
          <>
            <div className="ms-2 me-auto ">
              <div className="fw-bold">
                {`Voie ${voie.numVoie} : ${voie.interdite ? 'Interdite' : 'Libre'}`}
              </div>
            </div>
            <ButtonGroup size="sm">
              {
              onSwitchInterdite && (
                <Button onClick={onSwitchInterdite} variant="warning">{voie.interdite ? 'Autoriser' : 'Interdire'}</Button>
              )
              }
              {
              onDelete && (
                <Button onClick={onDelete} variant="danger">Supprimer</Button>
              )
              }
            </ButtonGroup>
          </>
        )
      }

    </ListGroup.Item>
  );
}

VoieListItem.propTypes = {
  voie: MPropTypes.objectOrObservableObject.isRequired,
  rame: MPropTypes.objectOrObservableObject,
  onSelect: PropTypes.func,
  onDelete: PropTypes.func,
  onSwitchInterdite: PropTypes.func,
};

VoieListItem.defaultProps = {
  rame: null,
  onSelect: null,
  onDelete: null,
  onSwitchInterdite: null,
};

export default observer(VoieListItem);
