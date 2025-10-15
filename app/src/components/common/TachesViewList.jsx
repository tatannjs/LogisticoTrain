import React from 'react';
import PropTypes from 'prop-types';
// import classNames from 'classnames';
import { observer, PropTypes as MPropTypes } from 'mobx-react';
import { ListGroup } from 'react-bootstrap';

function TachesViewList({
  taches, canSelect, selectedTaches, onSwitchSelect,
}) {
  const selectedTIdx = canSelect ? new Set(selectedTaches?.map((t) => t.idx)) : new Set();

  if (!canSelect) {
    return (
      <ListGroup>
        {
        taches.map((t) => (
          <ListGroup.Item key={t.idx}>{t.tache}</ListGroup.Item>
        ))
        }
      </ListGroup>
    );
  }

  return (
    <ListGroup>
      {
        taches.map((t) => (
          <ListGroup.Item
            key={t.idx}
            action
            active={selectedTIdx.has(t.idx)}
            onClick={() => onSwitchSelect && onSwitchSelect(t)}
          >
            {t.tache}
          </ListGroup.Item>
        ))
      }
    </ListGroup>
  );
}

TachesViewList.propTypes = {
  taches: MPropTypes.arrayOrObservableArray.isRequired,
  canSelect: PropTypes.bool,
  selectedTaches: MPropTypes.arrayOrObservableArray,
  onSwitchSelect: PropTypes.func,
};

TachesViewList.defaultProps = {
  canSelect: false,
  selectedTaches: null,
  onSwitchSelect: null,
};

export default observer(TachesViewList);
