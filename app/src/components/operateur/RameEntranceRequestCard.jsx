import React from 'react';
import PropTypes from 'prop-types';
// import classNames from 'classnames';
import { observer, PropTypes as MPropTypes } from 'mobx-react';
import {
  Button, ButtonGroup, Card, Dropdown, DropdownButton, ListGroup,
} from 'react-bootstrap';

function RameEntranceRequestCard({
  rameRequest, numVoies, onReject, onAccept,
}) {
  return (
    <Card border="primary" style={{ width: '100%' }}>
      <Card.Header>
        Demande de
        {rameRequest.auteur}
      </Card.Header>
      <Card.Body>
        <Card.Title>
          Rame N°
          {rameRequest.numSerie}
        </Card.Title>
        <Card.Text>
          Type de rame :
          {' '}
          {rameRequest.typeRame}
        </Card.Text>
      </Card.Body>
      <ListGroup className="list-group-flush">
        {
          rameRequest.taches?.map((tache) => (
            <ListGroup.Item key={tache}>{tache}</ListGroup.Item>
          ))
        }
      </ListGroup>
      <Card.Body>
        <ButtonGroup>
          <Button variant="danger" onClick={onReject}>Rejeter</Button>
          <DropdownButton
            as={ButtonGroup}
            title="Accepter"
            id={`${rameRequest.numSerie}-options-accept-voies`}
            variant="success"
            disabled={!numVoies.length}
          >
            {
              numVoies.map((numVoie, idx) => (
                <Dropdown.Item key={numVoie} eventKey={idx + 1} onClick={() => onAccept(numVoie)}>
                  Voie&nbsp;
                  {numVoie}
                </Dropdown.Item>
              ))
            }
          </DropdownButton>
        </ButtonGroup>
      </Card.Body>
    </Card>
  );
}

RameEntranceRequestCard.propTypes = {
  rameRequest: MPropTypes.objectOrObservableObject.isRequired,
  numVoies: MPropTypes.arrayOrObservableArray.isRequired,
  onReject: PropTypes.func.isRequired,
  onAccept: PropTypes.func.isRequired,
};

export default observer(RameEntranceRequestCard);
