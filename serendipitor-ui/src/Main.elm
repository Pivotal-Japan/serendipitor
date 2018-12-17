module Main exposing (Model(..), Msg(..), Participant, decodeParticipants, getParticipants, init, main, subscriptions, update, view, viewParticipant, viewParticipants)

import Browser
import Html exposing (..)
import Html.Attributes exposing (href)
import Html.Events exposing (onClick)
import Http
import Json.Decode exposing (Decoder, field, list, map2, string)



-- MAIN


main =
    Browser.element
        { init = init
        , update = update
        , subscriptions = subscriptions
        , view = view
        }



-- MODEL


type alias Participant =
    { userId : String
    , userName : String
    }


type Model
    = Failure
    | Loading
    | Success (List Participant)


init : () -> ( Model, Cmd Msg )
init _ =
    ( Loading, getParticipants )



-- UPDATE


type Msg
    = Reload
    | Join
    | Leave
    | Choose
    | GotParticipants (Result Http.Error (List Participant))
    | Refresh (Result Http.Error ())


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        Reload ->
            ( Loading, getParticipants )

        Join ->
            ( Loading, addMe )

        Leave ->
            ( Loading, removeMe )

        Refresh _ ->
            ( Loading, getParticipants )

        Choose ->
            ( Loading, choose )

        GotParticipants result ->
            case result of
                Ok users ->
                    ( Success users, Cmd.none )

                Err _ ->
                    ( Failure, Cmd.none )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none



-- VIEW


view : Model -> Html Msg
view model =
    div []
        [ h2 [] [ text "1on1 Participants" ]
        , viewParticipants model
        ]


viewParticipant : Participant -> Html Msg
viewParticipant user =
    li [] [ text user.userName ]


viewParticipants : Model -> Html Msg
viewParticipants model =
    case model of
        Failure ->
            div []
                [ a [ href "/index.html" ] [ text "Reload" ] ]


        Loading ->
            text "Loading..."

        Success users ->
            div []
                [ button [ onClick Join ] [ text "Join" ]
                , button [ onClick Leave ] [ text "Leave" ]
                , button [ onClick Reload ] [ text "Reload" ]
                , button [ onClick Choose ] [ text "Choose" ]
                , ul [] (List.map viewParticipant users)
                ]



-- HTTP


addMe : Cmd Msg
addMe =
    Http.post
        { url = "/me"
        , body = Http.emptyBody
        , expect = Http.expectWhatever Refresh
        }


removeMe : Cmd Msg
removeMe =
    Http.request
        { method = "DELETE"
        , headers = []
        , url = "/me"
        , body = Http.emptyBody
        , expect = Http.expectWhatever Refresh
        , timeout = Nothing
        , tracker = Nothing
        }


choose : Cmd Msg
choose =
    Http.post
        { url = "/"
        , body = Http.emptyBody
        , expect = Http.expectWhatever Refresh
        }


getParticipants : Cmd Msg
getParticipants =
    Http.get
        { url = "/slackusers"
        , expect = Http.expectJson GotParticipants decodeParticipants
        }


decodeParticipants : Decoder (List Participant)
decodeParticipants =
    list (map2 Participant (field "userId" string) (field "userName" string))
