import React, { useReducer } from 'react';
import PropTypes from 'prop-types';
import {
  Button, Form, InputGroup, ListGroup,
} from 'react-bootstrap';

function reduceState(state, action) {
  switch (action?.type) {
    case 'add-new-tache':
      return { ...state, taches: [...state.taches, { id: state.cptIdx, tache: '' }], cptIdx: state.cptIdx + 1 };
    case 'replace-tache':
      return {
        ...state,
        taches: [
          ...state.taches.slice(0, action.idx),
          { id: state.taches[action.idx].id, tache: action.value },
          ...state.taches.slice(action.idx + 1),
        ],
      };
    case 'remove-tache':
      return { ...state, taches: state.taches.filter((t) => t.id !== action.tache.id) };
    case 'set-form-validated':
      return { ...state, formValidated: action.value };
    case 'set-disabled':
      return { ...state, disabled: action.value };
    case 'reset':
      return {
        taches: [], cptIdx: 0, formValidated: false, disabled: false,
      };
    default:
      throw new Error(`Unmanaged action type ${action?.type}`);
  }
}

function TacheAdder({ onSaveTaches, resetAfterSave, disableOnSaving }) {
  const [state, dispatch] = useReducer(
    reduceState,
    {
      taches: [], cptIdx: 0, formValidated: false, disabled: false,
    },
  );

  const handleSendRequest = (evt) => {
    evt.preventDefault();
    const form = evt.currentTarget;
    if (form.checkValidity() === false || !state.taches?.length) {
      evt.stopPropagation();
    } else {
      const p = onSaveTaches(state.taches.map((t) => t.tache));
      if (p instanceof Promise) {
        if (disableOnSaving) {
          dispatch({ type: 'set-disabled', value: true });
        }
        p.then(() => {
          if (resetAfterSave) {
            dispatch({ type: 'reset' });
          }
        }).finally(() => {
          if (disableOnSaving) {
            dispatch({ type: 'set-disabled', value: false });
          }
        });
      } else if (resetAfterSave) {
        dispatch({ type: 'reset' });
      }
    }
    dispatch({ type: 'set-form-validated', value: true });
  };

  return (
    <Form noValidate validated={state.formValidated} onSubmit={handleSendRequest} className="mb-4">
      <fieldset disabled={state.disabled}>
        <Form.Group className="mb-3" controlId="rameEntranceRequest.typeRame">
          <Form.Label>Tâches à réaliser</Form.Label>
          <ListGroup>
            {
                state.taches.length
                  ? state.taches.map((tache, idx) => (
                    <ListGroup.Item key={tache.id}>
                      <InputGroup>
                        <Form.Control
                          as="textarea"
                          aria-label="Une tâche"
                          rows={2}
                          required
                          value={tache.tache}
                          onChange={(evt) => dispatch({ type: 'replace-tache', idx, value: evt.target.value })}
                        />
                        <Button
                          variant="outline-danger"
                          type="button"
                          onClick={() => dispatch({ type: 'remove-tache', tache })}
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
                        isInvalid={state.formValidated}
                      />
                    </ListGroup.Item>
                  )
          }
            <ListGroup.Item type="button" action onClick={() => dispatch({ type: 'add-new-tache' })}>+</ListGroup.Item>
          </ListGroup>
        </Form.Group>
        <Button variant="success" type="submit">Ajouter les tâches</Button>
      </fieldset>
    </Form>
  );
}

TacheAdder.propTypes = {
  onSaveTaches: PropTypes.func.isRequired,
  resetAfterSave: PropTypes.bool,
  disableOnSaving: PropTypes.bool,
};

TacheAdder.defaultProps = {
  resetAfterSave: false,
  disableOnSaving: false,
};

export default TacheAdder;
