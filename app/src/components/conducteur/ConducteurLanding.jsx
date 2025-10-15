import React from 'react';
import { Link } from 'react-router-dom';
import classNames from 'classnames';
import { Button, Col, Row } from 'react-bootstrap';

function ConducteurLanding() {
  return (
    <Row className={classNames('align-items-center', 'justify-content-center', 'vh-full')}>
      <Col xs="auto">
        <Button as={Link} variant="primary" to="entree">Entree de rame</Button>
      </Col>
      <Col xs="auto">
        <Button as={Link} variant="warning" to="sortie">Sortie de rame</Button>
      </Col>
    </Row>
  );
}

export default ConducteurLanding;
