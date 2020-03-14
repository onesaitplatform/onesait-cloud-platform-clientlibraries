from enum import Enum


class RestHeaders(Enum):
    X_OP_APIKey = "X-OP-APIKey"
    ACCEPT_STR = "Accept"
    ACCEPT_ALL = "*/*"
    APP_JSON = "application/json"
    CONT_TYPE  = "Content-type"
    USER_AGENT = "User-Agent"
    AUTHORIZATION = "Authorization"