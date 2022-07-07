# dell-cmi

## API

> All the functionalities described below are available on `http://localhost:8080/swagger-ui.html`.

### Get server status

Returns server status.

```
GET /status
```

Request:

```
curl -L -X GET 'http://localhost:8080/status'
```

Response:

```
Service is running on door :8080. With safe Connection.
```

### Init connection with OWC

Init connection doing login request to OWC.


```
GET /init?username=&password=
```

| Parameters | Type | Requirement | Description     |
|------------|---|-----------|-----------------|
| `username` | String | mandatory | OWC's username. |
| `password` | String | mandatory | OWC's password. |

Request:

```
curl -L -X GET 'http://localhost:8080/init?username=myUser&password=myPassword'
```

Response:

```
<String representing OWC's page on /cs/idcplg?IdcService=GET_DOC_PAGE&Action=GetTemplatePage&Page=HOME_PAGE&Auth=Internet>
```

### Get User info

Return a String with the XML page of the route `/cs/idcplg?IdcService=GET_USER_INFO&IsSoap=1`.

> Important: this route only works after been done the /init request with the proper credentials.

```
GET /userInfo
```

Request:

```
curl -L -X GET 'http://localhost:8080/userInfo'
```

Response:

```
<String representing XML OWC's page on /cs/idcplg?IdcService=GET_USER_INFO&IsSoap=1>
```
