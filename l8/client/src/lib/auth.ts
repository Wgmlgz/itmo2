import { writable } from 'svelte/store'

var auth2
var googleUser

const { subscribe, set, update } = writable(null)
gapi.load('auth2', () => {
  auth2 = gapi.auth2.init({
    client_id: __CLIENT_ID__,
    scope: 'profile'
  })

  auth2.isSignedIn.listen((loggedIn) => {
    if (loggedIn) {
      const u = auth2.currentUser.get()
      const profile = u.getBasicProfile()
      update(() => ({
        profile: {
          id: profile.getId(),
          name: profile.getName(),
          image: profile.getImageUrl(),
          email: profile.getEmail()
        },
        token: u.getAuthResponse().id_token
      })
    } else {
      update(() => null)
    }
  })
})



export const user = {
  subscribe,
  signin,
  logout
}
