import {
  GET_CURRENT_USER_REQUEST, GET_CURRENT_USER_SUCCESS, GET_CURRENT_USER_FAILURE,
  GET_ACTIVE_PLAYERS_REQUEST, GET_ACTIVE_PLAYERS_SUCCESS, GET_ACTIVE_PLAYERS_FAILURE,
  GET_ALL_PLAYERS_REQUEST, GET_ALL_PLAYERS_SUCCESS, GET_ALL_PLAYERS_FAILURE,
  GET_PLAYER_REQUEST, GET_PLAYER_SUCCESS, GET_PLAYER_FAILURE
} from "./const";
import Paths from "../dicts/paths";
import {api} from "../config/api";

export const getCurrent = () => {
	return async (dispatch) => {

		dispatch({type: GET_CURRENT_USER_REQUEST});

		try {
			const result = await api.get(Paths.User.GetCurrent);
			dispatch({type: GET_CURRENT_USER_SUCCESS, payload: result.data});
		} catch (err) {
			console.warn(err);
			dispatch({type: GET_CURRENT_USER_FAILURE});
		}
	}
}

export const getActivePlayers = () => {
  return async (dispatch) => {

    dispatch({type: GET_ACTIVE_PLAYERS_REQUEST});

    try {
      const result = await api.get(Paths.User.GetActive);
      dispatch({type: GET_ACTIVE_PLAYERS_SUCCESS, payload: result.data});
    } catch (err) {
      console.warn(err);
      dispatch({type: GET_ACTIVE_PLAYERS_FAILURE});
    }
  }
}

export const getAllPlayers = () => {
  return async (dispatch) => {

    dispatch({type: GET_ALL_PLAYERS_REQUEST});

    try {
      const result = await api.get(Paths.User.GetAll);
      dispatch({type: GET_ALL_PLAYERS_SUCCESS, payload: result.data});
    } catch (err) {
      console.warn(err);
      dispatch({type: GET_ALL_PLAYERS_FAILURE});
    }
  }
}

export const getPlayer = (id) => {
  return async (dispatch) => {

    dispatch({type: GET_PLAYER_REQUEST});

    try {
      const result = await api.get(Paths.User.GetPlayer(id));
      dispatch({type: GET_PLAYER_SUCCESS, payload: result.data});
    } catch (err) {
      console.warn(err);
      dispatch({type: GET_PLAYER_FAILURE});
    }
  }
}

export const updateUsername = async (data) => {
  return await api.put(Paths.User.UpdateUsername, data);
}
export const updatePassword = async (data) => {
  return await api.put(Paths.User.UpdatePassword, data);
}