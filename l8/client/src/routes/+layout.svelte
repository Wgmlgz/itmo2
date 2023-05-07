<script context="module">
  import { waitLocale } from 'svelte-i18n';

  export async function preload() {
    // awaits for the loading of the 'en-US' and 'en' dictionaries
    return waitLocale();
  }
</script>

<script lang="ts">
  import 'virtual:uno.css';
  import '../app.scss';

  import Drawer, { AppContent, Content, Header, Title, Subtitle, Scrim } from '@smui/drawer';
  import TopAppBar, { Row, Section, Title as TopBarTitle } from '@smui/top-app-bar';
  import IconButton from '@smui/icon-button';
  import Checkbox from '@smui/checkbox';
  import FormField from '@smui/form-field';
  import Select, { Option } from '@smui/select';
  import '$lib/i18n.ts';

  import { _, locale, locales } from 'svelte-i18n';

  import Button, { Label } from '@smui/button';
  import List, { Item, Text, Graphic, Separator, Subheader } from '@smui/list';
  import Auth from '../lib/components/auth.svelte';
  let value = 'Orange';
  let open = false;
  let active = 'Inbox';
  function setActive(value: string) {
    active = value;
    open = false;
  }
</script>

<svelte:head>
  <title>{$_('page_title')}</title>
</svelte:head>

<Drawer variant="modal" bind:open>
  <Header>
    <Title>Super Mail</Title>
    <Subtitle>It's the best fake mail app drawer.</Subtitle>
  </Header>
  <Content>
    <List>
      <Item
        on:click={() => setActive('Star')}
        activated={active === 'Star'}
      >
        <Graphic class="material-icons" aria-hidden="true">star</Graphic>
        <Text>{$_('abobus')}</Text>
      </Item>
    </List>
  </Content>
</Drawer>

<Scrim />
<AppContent class="app-content">
  <main class="main-content">
    <TopAppBar variant="static" color="primary">
      <Row>
        <Section>
          <IconButton on:click={() => (open = !open)} class="material-icons">menu</IconButton>
          <TopBarTitle>{$_('i_hate_java')}</TopBarTitle>
        </Section>
        <Section align="end" toolbar class="flex gap-3">
          <Select bind:value={$locale} variant="filled" label={$_('change locale')}>
            {#each $locales as locale}
              <Option value={locale}>{locale}</Option>
            {/each}
          </Select>
          <Auth />
        </Section>
      </Row>
    </TopAppBar>

    <slot />
  </main>
</AppContent>
