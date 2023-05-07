<script lang="ts">
  import Select, { Option } from '@smui/select';
  import { onMount } from 'svelte';
  import axios from 'axios';
  import Cmd from '../lib/components/cmd.svelte';
  import type { Routes, CmdInfo, Product, TaggedProductArg } from '$lib/storage';
  import Table from '$lib/components/table.svelte';
  import { commands } from '$lib/utils';
  import Commands from '$lib/components/commands.svelte';
  import Paper from '@smui/paper/src/Paper.svelte';
  import Selection from '$lib/components/selection.svelte';
  import Cookies from 'js-cookie';

  let selected_product:
    | TaggedProductArg
    | {
        type: 'ProductArg';
        product: null;
      } = {
    type: 'ProductArg' as const,
    product: null
  };
</script>

<div class="m-4">
  {#if Cookies.get('idc')}
      <Table bind:selected_product={selected_product.product} />
      {#if selected_product.product !== null}
        <Selection bind:product={selected_product} />
      {/if}
      <Commands />
  {/if}
</div>
