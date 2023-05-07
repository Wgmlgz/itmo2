<script lang="ts">
  import Paper, { Title, Subtitle, Content } from '@smui/paper';
  import Button from '@smui/button';
  import axios from 'axios';
  import Str from './str.svelte';
  import Product from './product.svelte';
  import Person from './person.svelte';
  import type { Routes, CmdInfo, Arg, Packet } from '$lib/storage';
  import { onMount } from 'svelte';
  import Cookies from 'js-cookie';
  import { defaultArg, send } from '$lib/utils';
  import { _ } from 'svelte-i18n';
  import ArgInput from './arg.svelte';

  export let type: Routes;
  export let info: CmdInfo;
  export let update: () => Promise<void>;

  let args: Arg[] = [];

  const x = () => (args = info.args.map(defaultArg));
  $: if (type) x();

  let output: string = '';

  const ssend = async () => {
    output = await send(type || '', args);
    update()
  };
  onMount(async () => {
    await ssend();
  });
</script>

<Paper>
  <Title
    >{$_(info.name)}
    <Button variant="raised" on:click={ssend}>{$_('send')}</Button>
  </Title>
  <Subtitle>{$_(info.help)}</Subtitle>
  <Content class="flex gap-4">
    {#each info.args as arg, idx}
      <ArgInput bind:arg bind:value={args[idx]} />
    {/each}
  </Content>

  {#if output}
    <Paper>
      <Title>{$_('Output:')}</Title>
      <Content>
        {output}
      </Content>
    </Paper>
  {/if}
</Paper>
