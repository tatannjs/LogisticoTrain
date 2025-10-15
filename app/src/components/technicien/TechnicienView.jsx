import React, { useContext } from 'react';
// import PropTypes from 'prop-types';
// import classNames from 'classnames';
import {
  ListGroup, Row, Col, Button,
} from 'react-bootstrap';
import { observer } from 'mobx-react';
import RootStore from '../../RootStore';
import Waiting from '../common/Waiting';
import VoieListItem from '../common/VoieListItem';
import HistoryActionsViewList from '../common/HistoryActionsViewList';
import TachesViewList from '../common/TachesViewList';
import TacheAdder from './TacheAdder';

function TechnicienView() {
  const { main: mainStore } = useContext(RootStore);

  return (
    <Waiting
      waiting={mainStore.loadingData ? 'Connexion en cours' : null}
    >
      <Row>
        <Col xs={12} md={6} xl={4}>
          <h5>Rames au dépôt</h5>
          <ListGroup>
            {
            mainStore.voies.map((voie) => (
              <VoieListItem
                key={voie.numVoie}
                voie={voie}
                rame={voie.rame}
                onSelect={voie.rame ? () => mainStore.selectRame(voie.rame) : null}
              />
            ))
          }
          </ListGroup>
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
              <TachesViewList
                taches={mainStore.selectedRame.taches}
                canSelect
                selectedTaches={mainStore.selectedTasks}
                onSwitchSelect={(t) => mainStore.switchTaskSelection(t)}
              />
              <Button className="mt-2" varian="primary" onClick={() => mainStore.realizeTasks()} disabled={!mainStore.selectedTasks?.length}>
                Terminer ces
                {' '}
                {mainStore.selectedTasks?.length}
                {' '}
                tache(s)
              </Button>
              <h5 className="mt-5">Ajout de tâches</h5>
              <TacheAdder
                onSaveTaches={(tasks) => mainStore.createTasks(tasks)}
                disableOnSaving
                resetAfterSave
              />

            </Waiting>
          </div>
          )}
        </Col>
        <Col xs={12} md={6} xl={4}>
          {mainStore.selectedRame && (
          <div className="position-sticky">
            <Waiting
              waiting={mainStore.selectedRame.isloading ? 'Chargement en cours' : null}
              error={mainStore.selectedRame.loadingError
                ? mainStore.selectedRame.loadingError.toString() : null}
            >
              <h5>Historique des actions</h5>
              <HistoryActionsViewList actions={mainStore.selectedRame.actions} />
            </Waiting>
          </div>
          )}
        </Col>
      </Row>
    </Waiting>
  );
}

export default observer(TechnicienView);
