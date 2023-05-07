<script lang="ts">
  import { CountryArr, UnitOfMeasureArr, type Product } from '$lib/storage';
  import { send, nToColor } from '$lib/utils';
  import { onMount } from 'svelte';
  import 'handsontable/dist/handsontable.full.min.css';
  import type Handsontable from 'handsontable';
  import { _, date, time, locale } from 'svelte-i18n';
  import 'handsontable/dist/handsontable.full.min.css';
  import * as d3 from 'd3';
  import Paper, { Content, Title } from '@smui/paper';
  import LayoutGrid, { Cell } from '@smui/layout-grid';

  let table: Product[] = [];

  let hot: Handsontable | null = null;
  const process = (data: Product[]) => {
    if (data.length === 0) return;

    let idx = undefined;
    if (selected_product) {
      idx = table
        .map((x, i) => [x, i] as const)
        .filter(([x, idx]) => x === selected_product)[0]?.[1];
    }
    table = data;
    if (selected_product) {
      if (idx !== undefined) {
        table[idx] = selected_product;
      }
    }

    const svg = d3.select(el).attr('width', '100%').attr('height', '100%');
    svg
      .selectAll('circle')
      .data(data)
      .join(
        (enter) =>
          enter
            .append('circle')
            .attr('cx', (d) => d.coordinates.x || 0)
            .attr('cy', (d) => d.coordinates.y || 0)
            .attr('r', 20)
            .attr('fill', (d) => nToColor(d.userId || 666))
            .on('click', (_, d) => (selected_product = d))
            .call((enter) => enter.transition().duration(500).attr('r', 20)),
        (update) =>
          update
            .attr('fill', (d) => nToColor(d.userId || 666))
            .call((update) =>
              update
                .transition()
                .duration(500)
                .attr('cx', (d) => d.coordinates.x || 0)
                .attr('cy', (d) => d.coordinates.y || 0)
            ),
        (exit) => exit.call((exit) => exit.transition().duration(500).attr('r', 0).remove())
      );
  };
  // const subscribe = () => {
  //   const evtSource = new EventSource('http://localhost:8080/sse');
  //   evtSource.onmessage = (event) => {
  //     console.log(event);
  //     var data = JSON.parse(event.data);
  //     process(data);
  //   };
  //   evtSource.onerror = () => subscribe();
  // };
  onMount(async () => {
    // subscribe();
    const Handsontable = (await import('handsontable')).default;

    const { registerLanguageDictionary, enUS, esMX, ruRU } = await import('handsontable/i18n');

    registerLanguageDictionary(enUS);
    registerLanguageDictionary(esMX);
    registerLanguageDictionary(ruRU);

    hot = new Handsontable(container, {
      data: [],
      columns: [
        {
          type: 'numeric',
          editor: false,
          data: 'userId'
        },
        {
          type: 'numeric',
          editor: false,
          data: 'id'
        },
        {
          type: 'text',
          editor: false,
          data: 'name'
        },
        {
          type: 'numeric',
          editor: false,
          data: 'coordinates.x'
        },
        {
          type: 'numeric',
          editor: false,
          data: 'coordinates.y'
        },
        {
          type: 'date',
          editor: false,
          data: 'creationDate'
        },
        {
          type: 'numeric',
          editor: false,
          data: 'price'
        },
        {
          type: 'numeric',
          editor: false,
          data: 'manufactureCost'
        },
        {
          type: 'dropdown',
          source: UnitOfMeasureArr,
          editor: false,
          data: 'unitOfMeasure'
        },
        {
          type: 'text',
          data: 'owner.name',
          editor: false
        },
        {
          type: 'date',
          editor: false,
          data: 'owner.birthday'
        },
        {
          type: 'dropdown',
          source: CountryArr,
          editor: false,
          data: 'owner.nationality'
        }
      ],
      rowHeaders: true,
      filters: true,
      dropdownMenu: true,
      colHeaders: true,
      height: 'auto',
      editor: false,
      columnSorting: true,
      afterSelection(row, column, row2, column2, preventScrolling, selectionLayerLevel) {
        selected_product = table[row] || null;
      },
      licenseKey: 'non-commercial-and-evaluation'
    });

    setInterval(update, 5000);
    await update();
  });
  export let update = async () => {
    process(JSON.parse(await send('Show', [])));
  };

  $: if (selected_product) process(table);
  $: {
    const formateDateTime = (s: string) => `${$date(new Date(s))} ${$time(new Date(s))}`;
    hot?.updateData(
      table.map((x) => {
        const t = structuredClone(x);
        t.creationDate = formateDateTime(t.creationDate);
        t.owner.birthday = formateDateTime(t.owner.birthday);
        return t;
      })
    );
  }

  $: {
    hot?.updateSettings({
      language: $locale as string,
      nestedHeaders: [
        [
          { label: '', colspan: 3 },
          { label: $_('Coordinates'), colspan: 2 },
          { label: '', colspan: 4 },
          { label: $_('Owner'), colspan: 3 }
        ],
        [
          $_('User id'),
          $_('Id'),
          $_('Name'),
          $_('X'),
          $_('Y'),
          $_('Creation Date'),
          $_('Price'),
          $_('Manufacture Cost'),
          $_('Unit Of Measure'),
          $_('Name'),
          $_('Birthday'),
          $_('Nationality')
        ]
      ]
    });
  }

  let el: Element;
  let container: Element;
  export let selected_product: Product | null = null;
</script>

<Paper class="pb-15" variant="unelevated">
  <Title>{$_('Table')}</Title>
  <Content>
    <LayoutGrid>
      <Cell span={6}>
        <Paper class="h-full" variant="outlined">
          <div class="-mb-15" bind:this={container} />
        </Paper>
      </Cell>
      <Cell span={6}>
        <Paper class="h-full" variant="outlined">
          <svg bind:this={el} class="chart" />
        </Paper>
      </Cell>
    </LayoutGrid>
  </Content>
</Paper>
