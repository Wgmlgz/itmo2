<script lang="ts">
  import Select, { Option } from '@smui/select';
  import { onMount } from 'svelte';
  import axios from 'axios';
  import type { Routes, CmdInfo } from '$lib/storage';
  import Table from '$lib/components/table.svelte';
  import { commands } from '$lib/utils';
  import Paper, { Content, Title } from '@smui/paper';
  import Cmd from './cmd.svelte';
  import Chip, { Set, LeadingIcon, TrailingIcon, Text } from '@smui/chips';
  import {_} from 'svelte-i18n'

  let selected: Routes = 'Info';
  const chips = Object.entries(commands) as [Routes, CmdInfo][]
</script>

<Paper variant="unelevated">
  <Title>{$_('Commands')}</Title>
  <Content>
    <Set bind:value={selected} {chips} label={$_("Select Command")} let:chip>
      <Chip {chip} on:click={() => selected = chip[0]}>
        {$_(chip[1].name)}
      </Chip>
    </Set>
    {#if selected}
      {@const cmd = commands[selected]}
      {#if cmd}
        <Cmd info={cmd} type={selected} />
      {/if}
    {/if}
  </Content>
</Paper>
