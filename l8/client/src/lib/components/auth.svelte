<script lang="ts">
  import jwt_decode from 'jwt-decode';
  import type { CredentialResponse } from 'google-one-tap';
  import { onMount } from 'svelte';
  import Cookies from 'js-cookie';
  import IconButton from '@smui/icon-button';
  import { _ } from 'svelte-i18n';
  export let user: Record<string, any> | null = null;

  const readUser = () => {
    let t = Cookies.get('idc');
    if (t) user = JSON.parse(t);
    console.log(user);
  };

  readUser();

  const handleCredentialResponse = (response: CredentialResponse) => {
    const token: any = jwt_decode(response.credential);
    console.log(token);
    user = {
      ...token,
      login: token?.name
    };
    Cookies.set('idc', JSON.stringify(user));
    location.reload();
  };

  let button: HTMLElement;

  onMount(async () => {
    console.log('init:', google.accounts.id);
    google.accounts.id.initialize({
      client_id: '912173031442-q6p845quib3kqgdh7el48ihpo0nm8dld.apps.googleusercontent.com',
      callback: handleCredentialResponse
    });
    google.accounts.id.renderButton(
      button,
      { theme: 'outline', size: 'large', text: 'continue_with' } // customization attributes
    );
    // google.accounts.id.prompt(); // also display the One Tap dialog
  });

  const logout = async () => {
    Cookies.remove('idc');
    user = null;
    location.reload();
  };
</script>

{#if user !== null}
  <img class="w-10 h-10 rounded-[999px] mr-4" src={user.picture} alt="profile" />
  <p>{user.name}</p>
  <IconButton on:click={logout} class="material-icons">logout</IconButton>
{:else}
  <div bind:this={button} data-auto_select="true" />
{/if}
