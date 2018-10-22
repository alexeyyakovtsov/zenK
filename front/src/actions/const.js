export const ActionType = {
  User: {
    GET_CURRENT_USER_REQUEST: 'GET_CURRENT_USER_REQUEST',
    GET_CURRENT_USER_SUCCESS: 'GET_CURRENT_USER_SUCCESS',
    GET_CURRENT_USER_FAILURE: 'GET_CURRENT_USER_FAILURE',
  },
  Player: {
    GET_ALL_PLAYERS_REQUEST: 'GET_ALL_PLAYERS_REQUEST',
    GET_ALL_PLAYERS_SUCCESS: 'GET_ALL_PLAYERS_SUCCESS',
    GET_ALL_PLAYERS_FAILURE: 'GET_ALL_PLAYERS_FAILURE',

    GET_ACTIVE_PLAYERS_REQUEST: 'GET_ACTIVE_PLAYERS_REQUEST',
    GET_ACTIVE_PLAYERS_SUCCESS: 'GET_ACTIVE_PLAYERS_SUCCESS',
    GET_ACTIVE_PLAYERS_FAILURE: 'GET_ACTIVE_PLAYERS_FAILURE',

    GET_PLAYER_REQUEST: 'GET_PLAYER_REQUEST',
    GET_PLAYER_SUCCESS: 'GET_PLAYER_SUCCESS',
    GET_PLAYER_FAILURE: 'GET_PLAYER_FAILURE',

    APPEND_TO_PLAYERS_REQUEST: 'APPEND_TO_PLAYERS_REQUEST',
    APPEND_TO_PLAYERS_SUCCESS: 'APPEND_TO_PLAYERS_SUCCESS',
    APPEND_TO_PLAYERS_FAILURE: 'APPEND_TO_PLAYERS_FAILURE',

    APPEND_TO_ACTIVE_PLAYERS_REQUEST: 'APPEND_TO_ACTIVE_PLAYERS_REQUEST',
    APPEND_TO_ACTIVE_PLAYERS_SUCCESS: 'APPEND_TO_ACTIVE_PLAYERS_SUCCESS',
    APPEND_TO_ACTIVE_PLAYERS_FAILURE: 'APPEND_TO_ACTIVE_PLAYERS_FAILURE',

    GET_TOP_PLAYERS_REQUEST: 'GET_TOP_PLAYERS_REQUEST',
    GET_TOP_PLAYERS_SUCCESS: 'GET_TOP_PLAYERS_SUCCESS',
    GET_TOP_PLAYERS_FAILURE: 'GET_TOP_PLAYERS_FAILURE',
  },
  Game: {
    GET_ALL_GAMES_REQUEST: 'GET_ALL_GAMES_REQUEST',
    GET_ALL_GAMES_SUCCESS: 'GET_ALL_GAMES_SUCCESS',
    GET_ALL_GAMES_FAILURE: 'GET_ALL_GAMES_FAILURE',

    GET_LATEST_GAMES_REQUEST: 'GET_LATEST_GAMES_REQUEST',
    GET_LATEST_GAMES_SUCCESS: 'GET_LATEST_GAMES_SUCCESS',
    GET_LATEST_GAMES_FAILURE: 'GET_LATEST_GAMES_FAILURE',

    GET_PLAYER_GAMES_REQUEST: 'GET_PLAYER_GAMES_REQUEST',
    GET_PLAYER_GAMES_SUCCESS: 'GET_PLAYER_GAMES_SUCCESS',
    GET_PLAYER_GAMES_FAILURE: 'GET_PLAYER_GAMES_FAILURE',

    APPEND_TO_GAMES_REQUEST: 'APPEND_TO_GAMES_REQUEST',
    APPEND_TO_GAMES_SUCCESS: 'APPEND_TO_GAMES_SUCCESS',
    APPEND_TO_GAMES_FAILURE: 'APPEND_TO_GAMES_FAILURE',

    APPEND_TO_PLAYER_GAMES_REQUEST: 'APPEND_TO_PLAYER_GAMES_REQUEST',
    APPEND_TO_PLAYER_GAMES_SUCCESS: 'APPEND_TO_PLAYER_GAMES_SUCCESS',
    APPEND_TO_PLAYER_GAMES_FAILURE: 'APPEND_TO_PLAYER_GAMES_FAILURE',

    GET_GAMES_COUNT_PER_WEEKS_REQUEST: 'GET_GAMES_COUNT_PER_WEEKS_REQUEST',
    GET_GAMES_COUNT_PER_WEEKS_SUCCESS: 'GET_GAMES_COUNT_PER_WEEKS_SUCCESS',
    GET_GAMES_COUNT_PER_WEEKS_FAILURE: 'GET_GAMES_COUNT_PER_WEEKS_FAILURE',
  }
}
