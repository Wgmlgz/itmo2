<script lang="ts">
  import type { Arg } from '$lib/storage';
  import Person from './person.svelte';
  import Product from './product.svelte';
  import Str from './str.svelte';

  export let arg: Arg['type'];
  export let value: Arg;
</script>

{#if arg === 'StrArg' && value.type === 'StrArg'}
  <Str bind:value />
{:else if arg === 'ProductArg' && value.type === 'ProductArg'}
  <Product bind:value />
{:else if arg === 'PersonArg' && value.type === 'PersonArg'}
  <Person bind:value />
{/if}
