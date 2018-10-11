import React, {Component} from 'react';
import styled from 'styled-components';
import {updatePhoto} from '../../actions/user';
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';
import {getCurrent, getPlayer} from '../../actions';

import uploadIco from '../../shared/images/icons/upload.png';
import UserPhoto from '../../components-ui/user-photo';

class ProfilePhotoBlock extends Component {
  constructor(props) {
    super(props);
    this.state = {
      file: null,
    }
  }

  onChange = (e) => {
    this.setState({file: e.target.files[0]});
    const data = new FormData();
    data.append('file', e.target.files[0]);
    this.uploadPhoto(data);
  }

  uploadPhoto = async (data) => {
    try {
      await updatePhoto(data);
      this.props.actions.getCurrent();
      this.props.actions.getPlayer(this.props.player.id);
      this.props.setPhotoError(null)
    } catch (err) {
      this.props.setPhotoError(err.response.data.message);
    }
  }

  render() {
    const {player, isCurrent} = this.props;
    const id = player.iconName + Math.random();

    return (
      <ProfilePhoto>
        <UserPhoto photo={player.iconName}/>
        {
          isCurrent &&
          <Button htmlFor={id} >
            <input id={id} type="file" onChange={this.onChange}/>
          </Button>
        }
      </ProfilePhoto>
    )
  }
}

const mapStateToProps = (state) => { // eslint-disable-line no-unused-vars
  const props = {
    currentUser: state.user.current,
    player: state.player.player
  };
  return props;
}
const mapDispatchToProps = (dispatch) => {
  const actions = {
    getCurrent,
    getPlayer,
  };
  const actionMap = {actions: bindActionCreators(actions, dispatch)};
  return actionMap;
}

export default connect(mapStateToProps, mapDispatchToProps)(ProfilePhotoBlock);

const ProfilePhoto = styled.div`
  width: 150px;
  min-width: 150px;
  height: 150px;
  margin: 0 20px;
  position: relative;
  img {
    border-radius: 100%;
    width: 100%;
    height: 100%;
  }
`;

const Button = styled.label`
  cursor: pointer;
  border-radius: 100%;
  position: absolute;
  left: 0;
  top:0;
  bottom: 0;
  right: 0;
  
  input{
    display: none;
  }
  &:hover {
    background-color: rgba(0, 0, 0, 0.5);
    background-image: url(${uploadIco});
    background-repeat: no-repeat;
    background-position: center;
    background-size: 40px 40px;
  }
`;
