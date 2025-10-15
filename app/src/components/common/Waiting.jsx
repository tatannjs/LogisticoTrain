import React, {
  useEffect, useState,
} from 'react';
import { observer } from 'mobx-react';
import PropTypes from 'prop-types';
import { Alert } from 'react-bootstrap';

const DEBOUNCE_TM = 300;

function Waiting({
  waiting, error, children,
}) {
  const [showAlert, setShowAlert] = useState(false);

  useEffect(() => {
    const tmId = setTimeout(() => {
      setShowAlert(true);
    }, DEBOUNCE_TM);
    return () => {
      setShowAlert(false);
      clearTimeout(tmId);
    };
  }, [children]);

  if (error && showAlert) {
    return (
      <Alert variant="danger">
        <Alert.Heading>Erreur de connexion</Alert.Heading>
        <p>
          Erreur :&nbsp;
          {error}
        </p>
      </Alert>
    );
  }

  if (waiting && showAlert) {
    return (
      <Alert variant="info">
        <Alert.Heading>Connexion</Alert.Heading>
        <p>{waiting}</p>
      </Alert>
    );
  }

  return children;
}

Waiting.propTypes = {
  waiting: PropTypes.node,
  error: PropTypes.node,
  children: PropTypes.oneOfType([PropTypes.node,
    PropTypes.arrayOf(PropTypes.node)]).isRequired,
};

Waiting.defaultProps = {
  waiting: false,
  error: false,
};

export default observer(Waiting);
