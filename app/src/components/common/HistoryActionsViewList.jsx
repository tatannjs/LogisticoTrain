import React from 'react';
// import PropTypes from 'prop-types';
// import classNames from 'classnames';
import { observer, PropTypes as MPropTypes } from 'mobx-react';
import { ListGroup } from 'react-bootstrap';

function ActionView({ action }) {
  return (
    <ListGroup.Item
      as="li"
      className="d-flex justify-content-between align-items-start"
    >
      <div className="ms-2 me-auto">
        <div className="fw-bold">{`${action.action} le ${action.timestamp}`}</div>
        <div>
          {`${action.numSerie} - ${action.typeRame}`}
        </div>
        {
            action.taches?.length && (
              <ListGroup variant="flush">
                {
                  action.taches.map((t) => (
                    <ListGroup.Item key={t}>{t}</ListGroup.Item>
                  ))
                }
              </ListGroup>
            )
          }
      </div>
      <div>
        <div>{action.auteur}</div>
        {(action.voie ?? false) !== false && (<div>{action.voie}</div>)}
      </div>
    </ListGroup.Item>
  );
}

ActionView.propTypes = {
  action: MPropTypes.objectOrObservableObject.isRequired,
};

const ObsActionView = observer(ActionView);

function HistoryActionsViewList({ actions }) {
  return (
    <ListGroup>
      {
      actions.map((action) => (
        <ObsActionView key={action.id} action={action} />
      ))
      }
    </ListGroup>
  );
}

HistoryActionsViewList.propTypes = {
  actions: MPropTypes.arrayOrObservableArray.isRequired,
};

export default observer(HistoryActionsViewList);
